(ns joc.ch4)

(let [imadeuapi 3.14159265358979323846264338327950288419716939937M]
  (println (class imadeuapi))
  imadeuapi)
; java.math.BigDecimal
;=> 3.14159265358979323846264338327950288419716939937M

(let [butieatedit 3.14159265358979323846264338327950288419716939937]
  (println (class butieatedit))
  butieatedit)
; java.lang.Double
;=> 3.141592653589793

(def clueless 9)
(class clueless)

(class (+ clueless 9000000000000000))

(class (+ clueless 90000000000000000000))

(class (+ clueless 9.0))

(Integer/MAX_VALUE)

; (+ Integer/MAX_VALUE Integer/MAX_VALUE)
;=> java.lang.ArithmeticException: integer overflow

(unchecked-add Integer/MAX_VALUE Integer/MAX_VALUE)

(float 0.000000000000000000000000000000000000000000000000000000000000000000000001)

1.0E-430

(let [aprox-interval  (/ 209715 2097152) ; Patriot's approximate 0.1
      actual-interval (/ 1 10)            ; Clojure's accurate 0.1
      hours           (* 3600 100 10)
      actual-total    (double (* hours actual-interval))
      aprox-total     (double (* hours aprox-interval))]
  (- actual-total aprox-total))

(+ 0.1M 0.1M 0.1M 0.1 0.1M 0.1M 0.1M 0.1M 0.1M 0.1M)

(+ 0.1M 0.1M 0.1M 0.1M 0.1M 0.1M 0.1M 0.1M 0.1M 0.1M)

1.0E-4300000M

; 1.0E-43000000000000000000000000M

(def a  1.0e50)
(def b -1.0e50)
(def c 17.0e00)

(+ (+ a b) c)

(+ a (+ b c))

(let [a (float 0.1)
      b (float 0.2)
      c (float 0.3)]
  (=
    (* a (+ b c))
    (+ (* a b) (* a c))))

(def a (rationalize 1.0e50))
(def b (rationalize -1.0e50))
(def c (rationalize 17.0e00))

(+ (+ a b) c)

(+ a (+ b c))

(let [a (rationalize 0.1)
      b (rationalize 0.2)
      c (rationalize 0.3)]
  (=
    (* a (+ b c))
    (+ (* a b) (* a c))))

; rational? --> check if a given number is a rational
; rationalize --> convert number to a rational

(numerator (/ 123 10))
(denominator (/ 123 10))

; keywords ("symbolic identifiers") -- always refer to themselves
; (e.g. `:magma` always has value `:magma`); however, the symbol
; `ruins` may refer to any legal Clojure value or reference

(def population {:zombies 2700, :humans 9})

; keywords can be used as functions!
(:zombies population)

(println (/ (:zombies population)
            (:humans population))
         "zombies per capita")

(defn pour [lb ub]
  (cond
   (= ub :toujors) (iterate inc lb)
   :else (range lb ub)))

(pour 1 10)

; (pour 1 :toujours) ;; runs forever!

::not-in-ns
:user/not-in-ns

(defn do-blowfish [directive]
  (case directive
    :aquarium/blowfish (println "feed the fish")
    :crypto/blowfish   (println "encode the message")
    :blowfish          (println "not sure what to do")))

(ns crypto)
(user/do-blowfish :blowfish)
(user/do-blowfish ::blowfish)

(ns aquarium)
(user/do-blowfish :blowfish)
(user/do-blowfish ::blowfish)

(identical? :a :a)

(identical? 'goat 'goat)
(= 'goat 'goat)

(name 'goat)

(let [x 'goat y x] (identical? x y))

(let [x (with-meta 'goat {:ornery true})
      y (with-meta 'goat {:ornery false})]
  [(= x y)
   (identical? x y)
   (meta x)
   (meta y)])

(ns where-is)
(def a-symbol 'where-am-i)
a-symbol
(resolve 'a-symbol)
`a-symbol

(if (> 1 3)
  8
  9)

(defn best [f xs]
  (reduce #(if (f % %2) % %2) xs))

(best > [1 3 4 2 7 5 3])

#"an example pattern"

(class #"example")

#"(?i)yo"

(seq (.split #"," "one,two,three"))

(re-seq #"\w+" "one-two/three")

(re-seq #"\w*(\w)" "one-two/three")
