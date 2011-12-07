(ns joc.ch7)

(map [:chthon :phthor :beowulf :grendel] #{0 3})
(map [:chthon :phthor :beowulf :grendel] [0 3])

([:chthon :phthor :beowulf :grendel] 0)
([:chthon :phthor :beowulf :grendel] 2)

(def fifth (comp first rest rest rest rest))
(fifth [1 2 3 4 5])

((comp first) [1 2 3 4 5])
((comp first rest) [1 2 3 4 5])
((comp first rest rest) [1 2 3 4 5])

(doc comp)

(first [1 2 3 4 5])
(rest [1 2 3 4 5])


((comp first rest) [1 2 3 4])

(defn fnth [n]
  (apply comp
         (cons first
               (take (dec n) (repeat rest)))))

((fnth 5) '[a b c d e])

(map (comp keyword #(.toLowerCase %) name) '(a B C))

((partial + 5) 100 200)
;; this is equivalent to:
(#(apply + 5 %&) 100 200)

((complement even?) 2)
;; this is equivalent to:
((comp not even?) 2)

(let [truthiness (fn [v] v)]
  [((complement truthiness) true)
   ((complement truthiness) 42)
   ((complement truthiness) false)
   ((complement truthiness) nil)])

(defn join
  {:test (fn []
           (assert
            (= (join "," [1 2 3]) "1,2,3")))}
  [sep s]
  (apply str (interpose sep s)))

(use '[clojure.test :as t])
(t/run-tests)

; a higher-order function is a function that does at least one of the following:
; -- takes one or more functions as arguments
; -- returns a function as a result

(def plays [{:band "Burial",     :plays 979,  :loved 9}
            {:band "Eno",        :plays 2333, :loved 15}
            {:band "Bill Evans", :plays 979,  :loved 9}
            {:band "Magma",      :plays 2665, :loved 31}])

(def sort-by-loved-ratio (partial sort-by #(/ (:plays %) (:loved %))))

(sort-by-loved-ratio plays)
(sort-by #(:plays %) plays)

(defn columns [column-names]
  (fn [row]
    (vec (map row column-names))))

(sort-by (columns [:plays :loved :band]) plays)

((columns [:plays :loved :band]) {:plays "foo", :loved "bar", :band "har"})

;; pure functions: functions that conform to the following guidelines:
;; -- function always returns the same result, given some expected
;; args
;; -- functions doesn't cause any observable side-effects

(defn keys-apply [f ks m]
  "Takes a function, a set of keys, and a map and applies the function
   to the map on the given keys. A new map of the results of the function
   applied to the keyed entries is returned."
  (let [only (select-keys m ks)]
    (zipmap (keys only) (map f (vals only)))))

(keys-apply #(.toUpperCase %) #{:band} (plays 0))

(select-keys {:foo :bar, :car :har} [:foo])
(keys (select-keys {:foo :bar, :car :har} [:foo]))
(zipmap [:foo] [:tar])

(defn manip-map [f ks m]
  "Takes a function, a set of keys, and a map and applies
   the function to the map on the given keys.  A modified
   version of the original map is returned with the results
   of the function applied to each keyed entry."
  (conj m (keys-apply f ks m)))

(manip-map #(int (/ % 2)) #{:plays :loved} (plays 0))

;; warning: this function is not pure! it depends on the global
;; `plays` and thsu no longer exists outside of the bounds of time
(defn halve! [ks]
  (map (partial manip-map #(int (/ % 2)) ks) plays))

(halve! [:plays])

;; named arguments
;; -- can be used in Clojure using destructuring, with optional
;; arguments flag `&`

;; the code below is equivalent to the following Python:
;;     def slope(p1=(0,0), ps=(1,1)):
;;         return (float(p2[1] - p1[1])) / (p2[0] - p1[0])

(defn slope
  [& {:keys [p1 p2] :or {p1 [0 0] p2 [1 1]}}]
  (float (/ (- (p2 1) (p1 1))
            (- (p2 0) (p1 0)))))

(slope :p1 [4 15] :p2 [3 21])

(slope :p2 [2 1])

(slope)

(defn slope [p1 p2]
  {:pre [(not= p1 p2) (vector? p1) (vector? p2)]
   :post [(float? %)]}
  (/ (- (p2 1) (p1 1))
     (- (p2 0) (p1 0))))

;; (slope [10 10] [10 10])
;; (slope [10 1] '(1 20))
;; (slope [10 1] [1 20])
(slope [10.0 1] [1 20])

(defn put-things [m]
  (into m {:meat "beef" :veggie "broccoli"}))

(put-things {})

(defn vegan-constraints [f m]
  {:pre [(:veggie m)]
   :post [(:veggie %) (nil? (:meat %))]}
  (f m))

; (vegan-constraints put-things {:veggie "carrot"})

(defn balanced-diet [f m]
  {:post [(:meat %) (:veggie %)]}
  (f m))

(balanced-diet put-things {})

(defn finicky [f m]
  {:post [(= (:meat %) (:meat m))]}
  (f m))

; (finicky put-things {:meat "chicken"})

;; “When will you learn? Closures are a poor man’s object.”

(def times-two
  (let [x 2]
    (fn [y] (* y x))))

(times-two 4)
(times-two 5)

(def add-and-get
  (let [ai (java.util.concurrent.atomic.AtomicInteger.)]
    (fn [y] (.addAndGet ai y))))

(add-and-get 2)
(add-and-get 2)
(add-and-get 7)

(defn times-n [n]
  (let [x n]
    (fn [y] (* y x))))

((times-n 3) 4)
((times-n 4) 7)
(times-n 4)
(def times-four (times-n 4))
(times-four 10)

(defn times-n [n]
  (fn [y] (* y n)))

((times-n 4) 10)

(defn divisible [denom]
  (fn [num]
    (zero? (rem num denom))))

((divisible 3) 6)
((divisible 3) 7)

(filter even? (range 10))

(filter (divisible 4) (range 10))

(defn filter-divisible [denom s]
  (filter (fn [num] (zero? (rem num denom))) s))

(filter-divisible 4 (range 10))

(defn filter-divisible [denom s]
  (filter #(zero? (rem % denom)) s))

(filter-divisible 4 (range 10))
(filter-divisible 5 (range 20))

(def bearings [{:x  0, :y  1}   ; north
               {:x  1, :y  0}   ; east
               {:x  0, :y -1}   ; south
               {:x -1, :y  0}]) ; west

(defn forward [x y bearing-num]
  [(+ x (:x (bearings bearing-num)))
   (+ y (:y (bearings bearing-num)))])

(forward 5 5 0)
(forward 5 5 1)
(forward 5 5 2)

(defn bot [x y bearing-num]
  {:coords [x y]
   :bearing ([:north :east :south :west] bearing-num)
   :forward (fn [] (bot (+ x (:x (bearings bearing-num)))
                        (+ y (:y (bearings bearing-num)))
                        bearing-num))})

(:coords (bot 5 5 0))
(:bearing (bot 5 5 0))

(:coords ((:forward (bot 5 5 0))))

(defn bot [x y bearing-num]
  {:coords     [x y]
   :bearing    ([:north :east :south :west] bearing-num)
   :forward    (fn [] (bot (+ x (:x (bearings bearing-num)))
                           (+ y (:y (bearings bearing-num)))
                           bearing-num))
   :turn-right (fn [] (bot x y (mod (+ 1 bearing-num) 4)))
   :turn-left  (fn [] (bot x y (mod (- 1 bearing-num) 4)))})

(:bearing ((:forward ((:forward ((:turn-right (bot 5 5 0))))))))

(:coords ((:forward ((:forward ((:turn-right (bot 5 5 0))))))))

(defn mirror-bot [x y bearing-num]
  {:coords     [x y]
   :bearing    ([:north :east :south :west] bearing-num)
   :forward    (fn [] (mirror-bot (- x (:x (bearings bearing-num)))
                                  (- y (:y (bearings bearing-num)))
                                  bearing-num))
   :turn-right (fn [] (mirror-bot x y (mod (- 1 bearing-num) 4)))
   :turn-left  (fn [] (mirror-bot x y (mod (+ 1 bearing-num) 4)))})

                                        ; recursion

(defn pow [base exp]
  (if (zero? exp)
    1
    (* base (pow base (dec exp)))))

(pow 2 10)
(pow 1.01 925)

;; (pow 2 10000)
;; java.lang.StackOverflowError
;; this is doomed to overflow the stack because the recursive call is
;; trapped by the multiplication operation. ideal solution is a
;; tail-recursive version that uses the explicit `recur` form (thus
;; avoids stack consumption)
;; we perform the multiplication at a different point, such that the
;; recursive call occurs in the tail position
;; ---> uses helper function `kapow` that does the majority of the
;; work
;; ---> kapow uses an accumulator that holds the result of the
;; multiplication (`exp` no longer used as a multiplicative value --
;; functions as a decrementing counter, thus eliminating stack explosion)

(defn pow [base exp]
  (letfn [(kapow [base exp acc]
            (if (zero? exp)
              acc
              (recur base (dec exp) (* base acc))))]
    (kapow base exp 1)))

(pow 2 10000)

(doc letfn)

;; for functions generating sequences, the use of lazy-seq might be a
;; better choice than tail recursion, because the regular (mundane)
;; recursive definition is more natural/ understandable

                                        ; tail calls and recur

(defn gcd [x y]
  (cond
   (> x y) (gcd (- x y) y)
   (< x y) (gcd (- y x) x)
   :else x))

(defn gcd [x y]
  (cond
   (> x y) (recur (- x y) y)
   (< x y) (recur (- y x) x)
   :else x))

(gcd 10 34)

(defn elevator [commands]
  (letfn
      [(ff-open [[cmd & r]]
         "When the elevator is open on the 1st floor it can
          either close or be done."
         #(case cmd
            :close (ff-closed r)
            :done true
            false))
       (ff-closed [[cmd & r]]
         "When the elevator is closed on the 1st floor it can
          either open or go up."
         #(case cmd
            :open (ff-open r)
            :up (sf-closed r)
            false))
       (sf-closed [[cmd & r]]
         "When the elevator is closed on the 2nd floor it can
          either go down or open."
         #(case cmd
            :down (ff-closed r)
            :open (sf-open r)
            false))
       (sf-open [[cmd & r]]
         "When the elevator is open on the 2nd floor it can
          either close or be done."
         #(case cmd
            :close (sf-closed r)
            :done true
            false))]
    (trampoline ff-open commands)))

;; each state function returns a function returning a value rather
;; than directly returning the value --> so that `trampoline` can
;; maange the stack on mutually recursive calls

(doc trampoline)

(elevator [:close :open :close :up :open :open :done])
(elevator [:close :up :open :close :down :open :done])

                                        ; continuation-passing style

(defn fac-cps [n k]
  (letfn [(cont [v] (k (* v n)))]
    (if (zero? n)
      (k 1)
      (recur (dec n) cont))))

(defn fac [n]
  (fac-cps n identity))

(fac 10)

(defn mk-cps [accept? end-value kend kont]
  (fn [n]
    ((fn [n k]
       (let [cont (fn [v] (k (kont v n)))]
         (if (accept? n)
           (k end-value)
           (recur (dec n) cont))))
     n kend)))

(def fac (mk-cps zero? 1 identity #(* %1 %2)))
(fac 10)

(def tri (mk-cps zero? 1 dec #(+ %1 %2)))
(tri 10)

;; note: continuation-passing style not widespread in clojure
;; - no tail-call optimization, harder to track errors, not conducive
;; to parallelization

                                        ; A* Pathfinding

(defn neighbors
  ([size yx] (neighbours [[-1 0] [1 0] [0 -1] [0 1]] size yx))
  ([deltas size yx]
     (filter (fn [new-yx]
               (every? #(< -1 % size) new-yx))
             (map #(map + yx %) deltas))))

(def world [[  1   1   1   1   1]
            [999 999 999 999   1]
            [  1   1   1   1   1]
            [  1 999 999 999 999]
            [  1   1   1   1   1]])

;; this is our view of the world
;; *   1: flat ground
;; * 999: cyclopean mountains
;; what is the optimal path from the upper-left corner ([0 0]) to the
;; lower-right ([4 4])?

(defn estimate-cost [step-cost-est size y x]
  (* step-cost-est
     (- (+ size size) y x 2)))

(estimate-cost 900 5 0 0)
(estimate-cost 900 5 4 4)

(defn path-cost [node-cost cheapest-nbr]
  (+ node-cost
     ;; add cheapest neighbor cost, else 0
     (:cost cheapest-nbr 0)))

(path-cost 900 {:cost 1})

(defn total-cost [newcost step-cost-est size y x]
  (+ newcost
     (estimate-cost step-cost-est size y x)))

(total-cost 0 900 5 0 0)
(total-cost 1000 900 5 3 4)

(defn min-by [f coll]
  (when (seq coll)
    (reduce (fn [min this]
              (if (> (f min) (f this)) this min))
            coll)))

(min-by :cost [{:cost 100} {:cost 36} {:cost 9}])

(defn astar [start-yx step-est cell-costs]
  (let [size (count cell-costs)]
    (loop [steps 0
           routes (vec (replicate size (vec (replicate size nil))))
           work-todo (sorted-set [0 start-yx])]
      (if (empty? work-todo)
        [(peek (peek routes)) :steps steps]
        (let [[_ yx :as work-item] (first work-todo)
              rest-work-todo (disj work-todo work-item)
              nbr-yxs (neighbors size yx)
              cheapest-nbr (min-by :cost
                                   (keep #(get-in routes %)
                                         nbr-yxs))
              newcost (path-cost (get-in cell-costs yx)
                                 cheapest-nbr)
              oldcost (:cost (get-in routes yx))]
          (if (and oldcost (>= newcost oldcost))
            (recur (inc steps) routes rest-work-todo)
            (recur (inc steps)
                   (assoc-in routes yx
                             {:cost newcost
                              :yxs (conj (:yxs cheapest-nbr [])
                                         yx)})
                   (into rest-work-todo
                         (map
                          (fn [w]
                            (let [[y x] w]
                              [(total-cost newcost step-est size y x) w]))
                          nbr-yxs)))))))))

;; (astar [0 0]
;;       900
;;       world)