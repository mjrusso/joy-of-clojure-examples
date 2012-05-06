(ns joc.ch11)

;; Clojure has four major mutable references:
;; - Refs:   synchronous coordination of multiple objects
;; - Agents: asynchronous actions
;; - Atoms:  lone, synchronous objects
;; - Vars:   thread-local storage
;; All but Vars are considered shared references (and allow for
;; changes to be seen across threads of execution).
;; Values can be accessed via the `@` reader macro, or the `deref`
;; function (regardless of the reference type).

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

;; Refs
;; Refs are coordinated (reads and writes to mulitple refs can be made
;; in a way that guarantees no race conditions).
;; Refs are retriable (the work done to update a ref's value is
;; speculative and may have to be repeated).

;; Clojure enforces that any change to a Ref's value occurs in a
;; transaction, maintaining a consistent view of the referenced value
;; in all threads.

;; A 3x3 chess board. The act of moving a piece requires a
;; *coordinated* change in two reference squares.
(def initial-board
  [[:- :k :-]
   [:- :- :-]
   [:- :K :-]])

(defn board-map [f bd]
  (vec (map #(vec (for [s %] (f s))) bd)))

(defn reset!
  "Resets the board state.  Generally these types of functions are a
   bad idea, but matters of page count force our hand."
  []
  (def board (board-map ref initial-board))
  (def to-move (ref [[:K [2 1]] [:k [0 1]]]))
  (def num-moves (ref 0)))

(defn neighbors
  ([size yx] (neighbors [[-1 0] [1 0] [0 -1] [0 1]] size yx))
  ([deltas size yx]
     (filter (fn [new-yx]
               (every? #(< -1 % size) new-yx))
     (map #(map + yx %) deltas))))

(def king-moves (partial neighbors
                         [[-1 -1] [-1 0] [-1 1] [0 -1] [0 1] [1 -1] [1 0] [1 1]] 3))

(defn good-move? [to enemy-sq]
  (when (not= to enemy-sq) to))

(defn choose-move [[[mover mpos][_ enemy-pos]]]
  [mover (some #(good-move? % enemy-pos)
               (shuffle (king-moves mpos)))])

(reset!)
(take 5 (repeatedly #(choose-move @to-move)))
@to-move

(defn place [from to] to)

(defn move-piece [[piece dest] [[_ src] _]]
  (alter (get-in board dest) place piece)
  (alter (get-in board src ) place :-)
  (alter num-moves inc))

(defn update-to-move [move]
  (alter to-move #(vector (second %) move)))

(defn make-move []
  (dosync
    (let [move (choose-move @to-move)]
      (move-piece move @to-move)
      (update-to-move move))))

(make-move)
(board-map deref board)
(deref num-moves)
@num-moves

(defn go [move-fn threads times]
  (dothreads! move-fn :threads threads :times times))

(go make-move 100 100)
(board-map #(dosync (deref %)) board)
@to-move
@num-moves

;; Bad: we shouldn't allow the Ref updates to happen in separate
;; transactions
(defn bad-make-move []
  (let [move (choose-move @to-move)]
    (dosync (move-piece move @to-move))
    (dosync (update-to-move move))))

(go bad-make-move 100 100)
(board-map #(dosync (deref %)) board)

;; Using `alter` can cause a transation to retry if a Ref it depends
;; on is modified and committed while it is running. `commute` can be
;; used in cases where the value of a Ref within the transaction is
;; not important to its completion semantics (e.g. `num-moves`, which
;; is a simple counter: its value at any given time is not important,
;; as long as it is incremented properly at the end of the transaction).
(doc commute)

(defn move-piece [[piece dest] [[_ src] _]]
  (commute (get-in board dest) place piece)
  (commute (get-in board src ) place :-)
  (commute num-moves inc))

(reset!)
(go make-move 100 100)
(board-map deref board)
@to-move

;; Use `commute` only if the following is acceptable:
;; - the value seen in-transaction may not be the value committed at
;;   commit time
;; - the function given to commute will be run at least twice (first,
;;   to compute the in-transaction value, and again to commute the
;;   commit value)

;; `ref-set` changes a value of a Ref to the raw value provided
;; (differs from `alter` and `commute`, which change a Ref based on a
;; function of its value). Not recommended.

(dosync (ref-set to-move '[[:K [2 1]] [:k [0 1]]]))
@to-move

;; Snapshot isolation: within a transaction, all enclosed Ref states
;; represent the same moment in time. Any Ref value inside a
;; transaction will never change, unless you explicitly change it
;; within that transaction. (If the values have changed, then the
;; transaction retries.)
;; Write skew: occurs when a decision is made inside of a transaction,
;; based on the in-transaction value of a Ref that is never written
;; to, is changed at the same time. The `ensure` function can be used
;; to avoid write skew.
;; `ensure` guarantees that a read-only Ref is not modified in another
;; thread.
(doc ensure)

;; As a rule of thumb, avoid having both short- and long-running
;; transactions interacting with the same Ref.

;; Stress a Ref -- mixes long-running (slow) transactions and quick
;; transactions.
(defn stress-ref [r]
  (let [slow-tries (atom 0)]
    (future
      (dosync
       (swap! slow-tries inc)
       (Thread/sleep 200)
       @r)
      (println (format "r is: %s, history: %d, after: %d tries"
                       @r (ref-history-count r) @slow-tries)))
    (dotimes [i 500]
      (Thread/sleep 10)
      (dosync (alter r inc)))
    :done))

(stress-ref (ref 0))
(stress-ref (ref 0 :max-history 30))
(stress-ref (ref 0 :min-history 15 :max-history 30))

(def my-ref-1 (ref 9))
(def my-ref-2 (ref 2)))))))
(dosync
 (alter my-ref-1 inc)
 (alter my-ref-2 - 3 5)
 (alter my-ref-2 inc))
(map deref [my-ref-1 my-ref-2])

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

