(ns joc.ch3)

; all truthy
(if true :truthy :falsey)
(if [] :truthy :falsey)
(if 0 :truthy :falsey)

; all falsey (only nil and false are actually false)
; (every object is "true" all the time, unless its nil or false)
(if nil :truthy :falsey)
(if false :truthy :falsey)

(when (nil? nil) "Actually nil, not false")

(nil? ())

(nil? (seq ()))

(seq [1 2 3])

(seq [])

(prn 4)

(defn print-seq [s]
  (when (seq s)
    (prn (first s))
    (recur (rest s))))

(print-seq [1 2 3 4 5 6])

(print-seq [])

(seq nil)

(def guys-whole-name ["Guy" "Lewis" "Steele"])

(str (nth guys-whole-name 2) ", "
     (nth guys-whole-name 0) " "
     (nth guys-whole-name 1))

(let [[f-name m-name l-name] guys-whole-name]
  (str l-name ", " f-name " " m-name))

(range 0 10)

(range 10)

(let [[a b c & more] (range 10)]
  (println "a b c are:" a b c)
  (println "more is:" more))

(vec (range 10))

(let [range-vec (vec (range 10))
      [a b c & more :as all] range-vec]
  (println "a b c are:" a b c)
  (println "more is:" more)
  (println "all is:" all))

(def guys-name-map
  {:f-name "Guy" :m-name "Lewis" :l-name "Steele"})

(let [{f-name :f-name, m-name :m-name, l-name :l-name} guys-name-map]
  (str l-name ", " f-name " " m-name))

(let [{:keys [f-name m-name l-name]} guys-name-map]
  (str l-name ", " f-name " " m-name))

(let [{f-name :f-name, :as whole-name} guys-name-map]
  whole-name)

(let [{:keys [title f-name m-name l-name], :or {title "Mr."}} guys-name-map]
  (println title f-name m-name l-name))

(let [{first-thing 0, last-thing 3} [1 2 3 4]]
  [first-thing last-thing])

(defn print-last-name [{:keys [l-name]}]
  (println l-name))

(print-last-name guys-name-map)

; 3.4 - Using the REPL to experiment

(range 5)

(for [x (range 2)
      y (range 2)]
  [x y])

;=> ([0 0] [0 1] [1 0] [1 1])

(find-doc "xor")

(bit-xor 1 2)

(for [x (range 2)
      y (range 2)]
  [x y (bit-xor x y)])

(defn xors [max-x max-y]
  (for [x (range max-x)
        y (range max-y)]
    [x y (bit-xor x y)]))

(xors 2 2)

(def frame (java.awt.Frame.))

frame

(for [method (seq (.getMethods java.awt.Frame))
      :let [method-name (.getName method)]
      :when (re-find #"Vis" method-name)]
  method-name)

(.isVisible frame)

(.setVisible frame true)

(.setSize frame (java.awt.Dimension. 200 200))

; (javadoc frame)

(find-doc "javadoc")

(def gfx (.getGraphics frame))

(.fillRect gfx 100 100 50 75)

(.setColor gfx (java.awt.Color. 255 128 0))
(.fillRect gfx 100 150 75 50)

(doseq [[x y xor] (xors 200 200)]
  (.setColor gfx (java.awt.Color. xor xor xor))
  (.fillRect gfx x y 1 1))

; (doseq [[x y xor] (xors 500 500)]
;   (.setColor gfx (java.awt.Color. xor xor xor))
;   (.fillRect gfx x y 1 1))

; (.printStackTrace *e)

(defn xors [max-x max-y]
  (for [x (range max-x)
        y (range max-y)]
    [x y (rem (bit-xor x y) 256)]))

(defn clear [g] (.clearRect g 0 0 200 200))

(clear gfx)

(doseq [[x y xor] (xors 200 200)]
  (.setColor gfx (java.awt.Color. xor xor xor))
  (.fillRect gfx x y 1 1))

(defn f-values [f xs ys]
  (for [x (range xs) y (range ys)]
    [x y (rem (f x y) 256)]))

(defn draw-values [f xs ys]
  (clear gfx)
  (.setSize frame (java.awt.Dimension. xs ys))
  (doseq [[x y v] (f-values f xs ys)]
    (.setColor gfx (java.awt.Color. v v v))
    (.fillRect gfx x y 1 1)))

(draw-values bit-xor 256 256)
(draw-values bit-and 256 256)
(draw-values + 256 256)
(draw-values * 256 256)

