(ns joc.ch11)

;; Clojure has four major mutable references:
;; - Refs:   synchronous coordination of multiple objects
;; - Agents: asynchronous actions
;; - Atoms:  lone, synchronous objects
;; - Vars:   thread-local storage
;; All but Vars are considered shared references (and allow for
;; changes to be seen across threads of execution).

(doc io!)

(io! (.println System/out "Haikeeba!"))

(doc dosync)

(dosync (.println System/out "Haikeeba!"))

;; Use the io! macro whenever performing I/O...
;; java.lang.IllegalStateException (I/O in transaction)
;; (dosync (io! (.println System/out "Haikeeba!")))

;; Used to help illustrate some examples.

(import '(java.util.concurrent Executors))
(def *pool* (Executors/newFixedThreadPool
            (+ 2 (.availableProcessors (Runtime/getRuntime)))))
(defn dothreads! [f & {thread-count :threads
                       exec-count :times
                      :or {thread-count 1 exec-count 1}}]
  (dotimes [t thread-count]
    (.submit *pool* #(dotimes [_ exec-count] (f)))))

;; TODO come back to 11.2 Refs

;; Agents

;; Each agent has a queue to hold actions that need to be performed
;; on its value. Each action will produce a new value for the agent to
;; hold and pass on to the subsequent action.

(def log-agent (agent 0))

(defn do-log [msg-id message]
  (println msg-id ":" message)
  (inc msg-id))

(defn do-step [channel message]
  (Thread/sleep 1)
  (send-off log-agent do-log (str channel message)))

(defn three-step [channel]
  (do-step channel " ready to begin (step 0)")
  (do-step channel " warming up (step 1)")
  (do-step channel " really getting going now (step 2)")
  (do-step channel " done! (step 3)"))

(defn all-together-now []
  (dothreads! #(three-step "alpha"))
  (dothreads! #(three-step "beta"))
  (dothreads! #(three-step "omega")))

(all-together-now)

@log-agent

(send log-agent (fn [_] 1000))

@log-agent

(do-step "epsilon" " near miss")

@log-agent

(defn exercise-agents [send-fn]
  (let [agents (map #(agent %) (range 10))]
    (doseq [a agents]
      (send-fn a (fn [_] (Thread/sleep 1000))))
    (doseq [a agents]
      (await a))))

(time (exercise-agents send-off))
(time (exercise-agents send))

(agent-error log-agent)

(send log-agent (fn [] 2000)) ; agent fails (incorrect)...

@log-agent

(agent-error log-agent)

;; Agent is failed, needs restart
;; (send log-agent (fn [_] 3000))

@log-agent

(restart-agent log-agent 2500 :clear-actions true)

@log-agent

(defn handle-log-error [the-agent the-err]
  (println "An action sent to the log-agent threw " the-err))
(set-error-handler! log-agent handle-log-error)
(set-error-mode! log-agent :continue)

;; Incorrect: cause error handler to be called
(send log-agent (fn [x] (/ x 0)))
(send log-agent (fn [] 0))

(send-off log-agent do-log "Stayin' alive, stayin' alive...")

;; Note: error handlers cannot change the state of the Agent.

;; Atoms
;; Synchronous like Refs, Uncoordinated like Agents

(def *time* (atom 0))
(defn tick [] (swap! *time* inc))
(dothreads! tick :threads 1000 :times 100)
@*time*

(def *time* (java.util.concurrent.atomic.AtomicInteger. 0))
(defn tick [] (.getAndIncrement *time*))
(dothreads! tick :threads 1000 :times 100)
*time*

;; Atoms must be used carefully within transactions. Once Atom's value
;; is set, it does not roll back, and transactions can potentially be
;; retried multiple times.
;; Thus, use Atoms only when an attempt to update its value is
;; idempotent.

;; Slight modification of the core memoize function to attach the Atom
;; to the function being memoized.
(defn manipulable-memoize [function]
  (let [cache (atom {})]
    (with-meta
      (fn [& args]
        (or (second (find @cache args))
            (let [ret (apply function args)]
              (swap! cache assoc args ret)
              ret)))
      {:cache cache})))

(def slowly (fn [x] (Thread/sleep 3000) x))
(time [(slowly 9) (slowly 9)])

(def sometimes-slowly (manipulable-memoize slowly))
(time [(sometimes-slowly 108) (sometimes-slowly 108)])

(meta sometimes-slowly)

(let [cache (:cache (meta sometimes-slowly))]
  (swap! cache dissoc '(108)))
(meta sometimes-slowly)

(time [(sometimes-slowly 108) (sometimes-slowly 108)])
(meta sometimes-slowly)

(reset! (:cache (meta sometimes-slowly)) {})
(meta sometimes-slowly)

