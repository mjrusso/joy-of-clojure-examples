(ns joc.ch5)

"You keep using that word. I do not think it means what you think it means."

; Clojure persistence: re: immutable in-memory collections

; Persistent collections in Clojure preserve historical versions of
; its state (all versions will have the same update and lookup
; complexity guarantees). Each instance of a collection is immutable
; and efficient.

; Java array -- NOT persistent

(def ds (into-array [:willie :barnabas :adam]))
(seq ds)
(aset ds 1 :quentin)
(seq ds)

; Clojure vector -- persistent

(def ds [:willie :barnabas :adam])
ds
(def ds1 (replace {:barnabas :quentin} ds))
ds
ds1

"It is better to have 100 functions operate on one data structure
than 10 functions on 10 data structures." ; Alan Perlis

; versus...

"It is better to have 100 functions operate on one data abstraction
than 10 functions on 10 data structures." ; Rich Hickey


; sequential, sequence, seq

; sequential collection: holds a series of values without reordering
; them

; sequence: a sequential collection that represents a series of values
; that may or may not exist yet

; seq: simple API for navigating collections -- note: a `seq` is any
; object that implements the seq API
; - first: returns first element in collection, otherwise nil
; - rest: returns a sequence of items other than the first,
;         otherwise empty sequence (never nil!)
; note: also a function called `seq` that accepts collection-like objects:
; calling seq on certain collections (e.g. lists) returns the
; collection itself, but usually returns a new seq object for
; navigating the collection. in either case, if the collection is
; empty, the seq returns nil and never an empty sequence

(seq [])
(seq ())

; there are three categories of composite data types:
; - sequentials
; - maps
; - sets

(= [1 2 3] '(1 2 3))
(= [1 2 3] #{1 2 3})

(class (hash-map :a 1))

(seq (hash-map :a 1))
(class (seq (hash-map :a 1)))

(seq (keys (hash-map :a 1)))
(class (keys (hash-map :a 1)))

(vec (range 10))

(let [my-vector [:a :b :c]]
  (into my-vector (range 10)))

(vector :a :b :c :d :e :f :g)

(map vector '(:a :b))

(def foovec [:a :b])

(into foovec (range 10))

foovec

(class (vector-of :int))

(into (vector-of :int) [Math/PI 2 1.3])

(into (vector-of :char) [100 101 102])

(count (vec (range 10)))

(map char (range 65 75))

(def a-to-j (vec (map char (range 65 75))))

a-to-j

(nth a-to-j 4)
(get a-to-j 4)
(a-to-j 4)

(nth a-to-j 19 :whoops)
(get a-to-j 19 :whoops)

(seq a-to-j)
(rseq a-to-j)

(assoc a-to-j 4 "no longer E")

(replace {2 :a, 4 :b} [1 2 3 2 3 4])

; assoc-in, get-in, update-in: all take series of indices to pick
; items from each more deeply nexted level

(def matrix
  [[1 2 3]
   [4 5 6]
   [7 8 9]])

(get-in matrix [1 2])

(assoc-in matrix [1 2] 'x)

(update-in matrix [1 2] * 100)

; given yx location in equilateral 2d matrix, returns sequence of
; locations surrounding it


(defn neighbours
  ([size yx] (neighbours [[-1 0] [1 0] [0 -1] [0 1]] size yx))
  ([deltas size yx]
     (filter (fn [new-yx]
               (every? #(< -1 % size) new-yx))
             (map #(map + yx %) deltas))))


(neighbours 3 [0 0])
(neighbours 3 [1 1])

; e.g. position 0,0 has neighbours 4,2 in `matrix`
(map #(get-in matrix %) (neighbours 3 [0 0]))

(map #(get-in matrix %) (neighbours 3 [1 1]))

(def my-stack [1 2 3])

(peek my-stack)

(pop my-stack)

(conj my-stack 4)

(+ (peek my-stack) (peek (pop my-stack)))

(defn strict-map2 [f coll]
  (loop [coll coll, acc []]
    (if (empty? coll)
      acc
      (recur (next coll) (conj acc (f (first coll)))))))

(strict-map2 - (range 5))

; subvectors: can be used to efficiently take a slice of an existing vector

a-to-j
(subvec a-to-j 3 6) ; note: exclusive: starts at 3, but ends *before* 6

(first {:width 10, :height 20, :depth 15})
(vector? (first {:width 10, :height 20, :depth 15}))

(conj [:a :b :c] (rest {:width 10, :height 20, :depth 15}))

(doseq [[dimension amount] {:width 10, :height 20, :depth 15}]
  (println (str (name dimension) ":") amount "inches"))

(name :hey)

                                        ; Lists

; lists are used almost exclusively to represent code forms (idiomatic clojure)
; (if the final usage of a collection isn't as clojure code, lists
; rarely offer any advantage over vectors, therefore rarely used)

(cons 1 '(2 3))
(conj '(2 3) 1)

; the "right way" to add to thre front of the list is with conj, not
; cons (this is a departure from classic lisps)

(def my-list-stack '(1 2 3))
(peek my-list-stack)
(pop my-list-stack)
(conj my-list-stack 9)

; persistent queues: FIFO -- conj adds to rear, pop removes from
; front, peek returns first element without removal

clojure.lang.PersistentQueue/EMPTY

(defmethod print-method clojure.lang.PersistentQueue
  [q, w]
  (print-method '<- w) (print-method (seq q) w) (print-method '-< w))

clojure.lang.PersistentQueue/EMPTY

(def schedule
  (conj clojure.lang.PersistentQueue/EMPTY
        :wake-up :shower :brush-teeth))

schedule

(peek schedule)

(pop schedule)

(#{:a :b :c :d} :c)

(#{:a :b :c :d} :e)

(get #{:a 1 :b 2} :b)

(get #{:a 1 :b 2} :nothing-doing)

; #{[] ()}

; #{[1 2] (1 2)}

; #{[] () #{} {}}

; frequently-used idiom for searching for containment within a
; sequence. `some` function takes a predicate and a sequence, applying
; the predicate to each element in turn, and returns the first truthy
; value returned by the predicate, else nil. sets are frequently used
; as predicates, because sets are functions of their elements that
; return the matched element, or nil.
(some #{:b} [:a 1 :b 2])
(some #{1 :b} [:a 1 :b 2])
(some #{9} [1 2 3 4 :a :b :c])

(sorted-set :b :c :a)
(sorted-set [3 4] [1 2])
(sorted-set [4 3] [1 2])

; (sorted-set :b 2 :c :a 3 1)
; clojure.lang.Keyword cannot be cast to java.lang.Number
; -- exception thrown because arguments to sorted-set not mutually comparable

(contains? #{1 2 4 3} 4)
(contains? [1 2 4 3] 4) ; false, because only returns true if a given
                        ; key exists within the collection

(clojure.set/intersection #{:humans :fruit-bats :zombies}
                          #{:chupacabra :zombies :humans})

(clojure.set/intersection #{:pez :gum :dots :skor}
                          #{:pez :skor :pocky}
                          #{:pocky :gum :skor})

(clojure.set/union #{:humans :fruit-bats :zombies}
                   #{:chupacabra :zombies :humans})

(clojure.set/union  #{:pez :gum :dots :skor}
                    #{:pez :skor :pocky}
                    #{:pocky :gum :skor})

(clojure.set/difference #{1 2 3 4} #{3 4 5 6})

(hash-map :a 1, :b 2, :c 3, :d 4, :e 5)

(let [m {:a 1, 1 :b, [1 2 3] "4 5 6"}]
  [(get m :a) (get m [1 2 3])])

(let [m {:a 1, 1 :b, [1 2 3] "4 5 6"}]
  [(m :a) (m [1 2 3])])

(seq {:a 1, :b 2})

(into {} [[:a 1] [:b 2]])

(into {} (map vec '[(:a 1) (:b 2)]))
(into {} (map vec ['(:a 1) '(:b 2)]))

(apply hash-map [:a 1 :b 2])

(zipmap [:a :b] [1 2])

(sorted-map :thx 1138 :r2d 2)

(sorted-map "bac" 2 "abc" 9)

(sorted-map-by #(compare (subs %1 1) (subs %2 1)) "bac" 2 "abc" 9)

(assoc {1 :int} 1.0 :float)

(assoc (sorted-map 1 :int) 1.0 :float)

(seq (hash-map :a 1, :b 2, :c 3))

(seq (array-map :a 1, :b 2, :c 3))

(defn index [coll]
  (cond
   (map? coll) (seq coll)
   (set? coll) (map vector coll coll)
   :else (map vector (iterate inc 0) coll)))

(seq {:a :b, :c :d})
(seq #{1 2 3 4})
(map vector #{1 2 3 4})
(map vector #{1 2 3 4} #{1 2 3 4})
(map vector (iterate inc 0) #{1 2 3 4})

(index [:a 1 :b 2 :c 3 :d 4])
(index {:a 1 :b 2 :c 3 :d 4})
(index #{:a 1 :b 2 :c 3 :d 4})

(defn pos [e coll]
  (for [[i v] (index coll) :when (= e v)] i))

(pos 3 [:a 1 :b 2 :c 3 :d 4])
(pos 3 {:a 1, :b 2, :c 3, :d 4})
(pos 3 [:a 3 :b 3 :c 3 :d 4])
(pos 3 {:a 3, :b 3, :c 3, :d 4})

(defn pos [pred coll]
  (for [[i v] (index coll) :when (pred v)] i))

(pos even? [2 3 6 7])
(pos #{3 4} {:a 1 :b 2 :c 3 :d 4})
(pos #{3} [:a 1 :b 2 :c 3 :d 4])
