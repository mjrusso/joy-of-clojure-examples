(ns joc.ch2)

42

"The Misfits"

:pyotr

(println :sym)

+9

-107

0x7F

2e3

1.17

22/7

100/4

-103/4

:ThisIsTheNameOfAKeyword

"This is a string"

"This is also a
    String"

\a

\u0042

\\

\u30DE

`(yankee hotel foxtrot)

`(1 2 3 4)

()

`(1 2 (3 4 5) 6)

[1 2 `(3 4 5) 6]

{1 :one, 2 :two}

{1 :one 2 :two}

#{1 2 "three" :four 0x5}

;; java.lang.IllegalArgumentException: Duplicate key: :four
;; #{1 2 "three" :four 0x5 :four 2 1}

(+ 1 2 3)

(fn [x y] #{x y})

(fn mk-set [x y] #{x y})

((fn [x y] #{x y}) 1 2)

((fn
  ([x]   #{x})
  ([x y] #{x y})) 42)

((fn arity2+ [x y & z] [x y z]) 1 43)

(def make-a-set
  (fn
    ([x]   #{x})
    ([x y] #{x y})))

(make-a-set 1 5)

(defn make-a-set
  "Takes either one or two values and makes a set from them"
  ([x]   #{x})
  ([x y] #{x y}))

(make-a-set 99)

(def make-a-list_ #(list %))
(def make-a-list1 #(list %1))
(def make-a-list3+ #(list %1 %2 %3 %&))

(make-a-list_ 2)
(make-a-list1 6)
(make-a-list3+ 1 2 3 4)
(make-a-list3+ 1 2 3 4 5 6)

(def x 42)

(println x)

x

(.start (Thread. #(println "Answer: " x)))

(def y)

(do
  6
  (+ 5 4)
  3)

(let [r         5
      pi        3.1415
      r-squared (* r r)]
  (println "radius is " r)
  (println "r is " r)
  (* pi r-squared))

(defn print-down-from [x]
  (when (pos? x)
    (println x)
    (recur (dec x))))

(print-down-from 4)

(defn sum-down-from [sum x]
  (if (pos? x)
    (recur (+ sum x) (dec x))
    sum))

(sum-down-from 0 5)

(defn sum-down-from [initial-x]
  (loop [sum 0
         x initial-x]
    (if (pos? x)
      (recur (+ sum x) (dec x))
      sum)))

(sum-down-from 6)

(cons 1 [2 3])

(def tena 9)

tena

;; (tena)

(quote tena)

(quote (cons 1 [2 3]))

(println (quote (cons 1 [2 3])))

(cons 1 (quote (2 3)))

(cons 1 '(2 3))

'(1 (+ 2 3))

(quote (1 (+ 2 3)))

()

`map

`Integer

`(map even? [1 2 3])

(map even? [1 2 3])

`(+ 10 (* 3 2))

`(+ 10 ~(* 3 2))

`~3

(let [x 2]
  `(1 ~x 3))

`(1 (2 3))

(let [x '(2 3)] `(1 ~x))

(let [x '(2 3)] `(1 ~@x))

`potion

`potion#

java.util.Locale/JAPAN

(Math/sqrt 9)

(new java.util.HashMap {"foo" 42 "bar" 9 "baz" "quux"})

(java.util.HashMap. {"foo" 42 "bar" 9 "baz" "quux"})

(.x (java.awt.Point. 10 20))

(.toString (java.awt.Point. 10 20))

(.divide (java.math.BigDecimal. "42") 2M)

(let [origin (java.awt.Point. 0 0)]
  (set! (.x origin) 15)
  (str origin))

(.endsWith (.toString (java.util.Date.)) "2011")

(.. (java.util.Date.) toString (endsWith "2011"))

(doto (java.util.HashMap.)
  (.put "HOME" "/home/me")
  (.put "SRC" "src")
  (.put "BIN" "classes"))

; (throw (Exception. "I done throwed"))

(defn throw-catch [f]
  [(try
     (f)
     (catch ArithmeticException e "No dividing by zero!")
     (catch Exception e (str "You are so bad " (.getMessage e)))
     (finally (println "returning...")))])

(throw-catch #(/ 10 5))
(throw-catch #(/ 10 0))
(throw-catch #(throw (Exception. "foo")))

(ns joy.ch1)

*ns*

(defn hello [] (println "Hello Cleveland!"))

(defn report-namespace [] (str "The current namespace is " *ns*))

(report-namespace)

hello

(ns joy.another)

; (report-namespace)

(ns joy.req
  (:require clojure.set))

(clojure.set/intersection #{1 2 3} #{3 4 5})

(ns joy.req-alias
  (:require [clojure.set :as s]))

(s/intersection #{1 2 3} #{3 4 5})

(ns joy.use-ex
  (:use [clojure.string :only [capitalize]]))

(map capitalize ["kilgore" "trout"])

; (ns joy.exclusion
;   (:use [clojure.string :exclude [capitalize]]))

; (map capitalize ["kilgore" "trout"])

(ns joy.yet-another
  (:refer joy.ch1 :rename {hello hi}))

(report-namespace)

(hi)

(ns joy.java
  (:import [java.util HashMap]
           [java.util.concurrent.atomic AtomicLong]))

(HashMap. {"happy?" true})

(AtomicLong. 42)

