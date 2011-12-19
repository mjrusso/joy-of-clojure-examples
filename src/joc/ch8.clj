(ns joc.ch8)

;; "If you give someone Fortran, he has Fortran. If you give someone
;; Lisp, he has any language he pleases." Guy Steele

(doc ->)

;; threaded form macro: think of this as an arrow indicating the flow
;; of data from one function to another (aka. "arrow macros")
(-> 25 Math/sqrt int list)
;; ... expands into the following expression
(list (int (Math/sqrt 25)))

;; -> : threads a sequence of forms as the first argument in the
;;      outermost expression

;; => : threads a sequence of forms as the last argument in the
;;      outermost expression


;; comparison of -> and => below. note that the commas are visual
;; markers of the "stitch" point. commas are simply treated as
;; whitespace and are used for educational purposes only in these examples

(-> (/ 144 12) (/ ,,, 2 3) str keyword list)
(list (keyword (str (/ (/ 144 12) 2 3))))

(-> (/ 144 12) (* ,,, 4 (/ 2 3)) str keyword (list ,,, :33))
(list (keyword (str (* (/ 144 12) 4 (/ 2 3)))) :33)

(->> a (+ 5 ,,,) (let [a 5] ,,,))
(let [a 5] (+ 5 a))

;; arrow macros demonstrate one potential use case for macros: taking
;; one form of an expression and transforming it into another form

(eval 42)
(eval '(list 1 2))
;; (eval (list 1 2))
(eval (list (symbol "+") 1 2))

;; an implementation of eval, taking a local context
(defn contextual-eval [ctx expr]
  (eval
   `(let [~@(mapcat (fn [[k v]] [k `~v]) ctx)]
      ~expr)))

(contextual-eval {'a 1, 'b 2} '(+ a b))

(contextual-eval {'a 1, 'b 2} '(let [b 1000] (+ a b)))

;; control structures -- most control structures implemented via
;; macros

(defmacro do-until [& clauses]
  (when clauses
    (list `when (first clauses)
          (if (next clauses)
            (second clauses)
            (throw (IllegalArgumentException.
                    "do-until requires an even number of forms")))
          (cons 'do-until (nnext clauses)))))

(do-until
 (even? 2) (println "Even")
 (odd?  3) (println "Odd")
 (zero? 1) (println "You never see me")
 :lollipip (println "Truthy thing"))

(do-until
 (even? 2) (println "Even")
 (odd?  3) (println "Odd")
 :lollipip (println "Truthy thing")
 (zero? 1) (println "You never see me"))

(macroexpand-1 '(do-until true (prn 1) false (prn 2)))

(require '[clojure.walk :as walk])
(walk/macroexpand-all '(do-until true (prn 1) false (prn 2)))

(defmacro unless [condition & body]
  `(if (not ~condition)
     (do ~@body)))

(unless (even? 3) "Now we see it...")
(unless (even? 2) "Now we don't.")

;; do: evaluates the expressions in order and returns the value of the
;; last; if no expressions are supplied, returns nil
(do "1" "2" "3" "4")

(macroexpand-1 '(unless (even? 5) "this is it"))

;; exploring what happens with a missing unquote (~)...
(macroexpand `(if (not condition) "got it"))
;; (eval `(if (not condition) "got it"))
(def condition false)
(eval `(if (not condition) "got it"))

;; "clojure macros work to mold the language into the problem space
;; rather than forcing you to mold the problem space into the
;; constructs of the language"

(defmacro def-watched [name & value]
  `(do
     (def ~name ~@value)
     (add-watch (var ~name)
                :re-bind
                (fn [~'key ~'r old# new#]
                  (println old# " -> " new#)))))

(def-watched x (* 12 12))
x

(def x 0)

(macroexpand-1 '(def-watched x (* 12 12)))

(defmacro domain [name & body]
  `{:tag :domain,
    :attrs {:name (str '~name)},
    :content [~@body]})

(doc declare)

(declare handle-things)

(defmacro grouping [name & body]
  `{:tag :grouping,
    :attrs {:name (str '~name)},
    :content [~@(handle-things body)]})

