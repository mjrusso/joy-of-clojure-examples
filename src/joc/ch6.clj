(ns joc.ch6)

; immutability: all of the possible properties of immutable objects
; are defined at the time of their construction and can't be changed
; thereafter

(def baselist (list :barnabas :adam))
(def lst1 (cons :willie baselist))
(def lst2 (cons :phoenix baselist))

lst1
lst2

(= (next lst1) (next lst2))

{:val 5, :L nil, :R nil}

(defn xconj [t v]
  (cond
   (nil? t) {:val v, :L nil, :R nil}))

(xconj nil 5)

(defn xconj [t v]
  (cond
   (nil? t)       {:val v, :l nil, :R nil}
   (< v (:val t)) {:val (:val t),
                   :L (xconj (:L t) v),
                   :R (:R t)}))

(def tree1 (xconj nil 5))
tree1

(def tree1 (xconj tree1 3))
tree1

(def tree1 (xconj tree1 2))
tree1

(defn xseq [t]
  (when t
    (concat (xseq (:L t)) [(:val t)] (xseq (:R t)))))

(xseq tree1)

(defn xconj [t v]
  (cond
   (nil? t)       {:val v, :l nil, :R nil}
   (< v (:val t)) {:val (:val t),
                   :L (xconj (:L t) v),
                   :R (:R t)}
   :else          {:val (:val t),
                   :L (:L t),
                   :R (xconj (:R t) v)}))

(def tree2 (xconj tree1 7))
(xseq tree2)

(identical? (:L tree1) (:L tree2))
(xseq (:L tree1))
(xseq (:L tree2))

(defn if-chain [x y z]
  (if x
    (if y
      (if z
        (do
          (println "Made it!")
          :all-truthy)))))

(if-chain () 42 true)
(if-chain true true false)

(defn and-chain [x y z]
  (and x y z (do (println "Made it!") :all-truthy)))

(and-chain () 42 true)
(and-chain true true false)

; (steps [1 2 3 4])
;=> [1 [2 [3 [4 []]]]]

(defn rec-step [[x & xs]]
  (if x
    [x (rec-step xs)]
    []))

(rec-step [1 2 3 4])

; rec-step will blow the stack on large sets -- with a lazy seq, we
; can apply laziness to our functions, and sidestep this issues. steps:

; 1. use the `lazy-seq` macro at the outermost level of your lazy
; sequence producing expression(s)
; 2. if consuming another sequence during operations, use `rest`
; instead of `next`
; 3. prefer high-order functions when processing sequences
; 4. don't hold onto your head

; rest vs next -- `rest` doesn't realize more elements than it needs
; to, but `next` does, because it needs to determine whether a seq is
; empty (using `next` causes a lazy seq to be one element less lazy)

(defn lz-rec-step [s]
  (lazy-seq
   (if (seq s)
     [(first s) (lz-rec-step (rest s))]
     [])))

(lz-rec-step [1 2 3 4])

(class (lz-rec-step [1 2 3 4]))

(dorun (lz-rec-step (range 200000)))

(defn simple-range [i limit]
  (lazy-seq
   (when (< i limit)
     (cons i (simple-range (inc i) limit)))))

(simple-range 0 9)

; (let [r (range 1e9)] [(first r) (last r)])

; warning: runs forever...
; (iterate (fn [n] (/ n 2) 1)
;=> (1 1/2 1/4 1/8 ...)

(defn triangle [n]
  (/ (* n (+ n 1)) 2))

(triangle 10)

(map triangle (range 1 11))

(def tri-nums (map triangle (iterate inc 1)))

(take 10 tri-nums)

(take 10 (filter even? tri-nums))

(nth tri-nums 99)

(double (reduce + (take 1000 (map / tri-nums))))
(reduce + (take 1000 (map / tri-nums)))

(take 2 (drop-while #(< % 10000) tri-nums))

; `delay`: defer evaluation of an expression until explicitly forced
; using the `force` function

(defn defer-expensive [cheap expensive]
  (if-let [good-enough (force cheap)]
    good-enough
    (force expensive)))

(defer-expensive
  (delay :cheap)
  (delay (do (Thread/sleep 5000) :expensive)))

(defer-expensive
  (delay false)
  (delay (do (Thread/sleep 5000) :expensive)))

(defn inf-triangles [n]
  {:head (triangle n)
   :tail (delay (inf-triangles (inc n)))})

(defn head [l] (:head l))
(defn tail [l] (force (:tail l)))

(def tri-nums (inf-triangles 1))

(head tri-nums)
(head (tail tri-nums))
(head (tail (tail tri-nums)))

(defn taker [n l]
  (loop [t n, src l, ret []]
    (if (zero? t)
      ret
      (recur (dec t) (tail src) (conj ret (head src))))))

(defn nthr [l n]
  (if (zero? n)
    (head l)
    (recur (tail l) (dec n))))

(taker 10 tri-nums)

(nthr tri-nums 99)

(ns joy.q)

(defn nom [n] (take n (repeatedly #(rand-int n))))

(defn sort-parts
  "Lazy, tail-recursive, incremental quicksort. Works against and
   creates partitions based on the pivot, defined as 'work'."
  [work]
  (lazy-seq
   (loop [[part & parts] work]
     (if-let [[pivot & xs] (seq part)]
       (let [smaller? #(< % pivot)]
         (recur (list*
                 (filter smaller? xs)
                 pivot
                 (remove smaller? xs)
                 parts)))
       (when-let [[x & parts] parts]
         (cons x (sort-parts parts)))))))

(defn qsort [xs]
  (sort-parts (list xs)))

(qsort [2 1 4 3])

(qsort (nom 20))

(first (qsort (nom 100)))

(take 10 (qsort (nom 10000)))

