(ns joc.ch9)

(doc in-ns)

(in-ns 'joy.ns)
(def authors ["Chouser"])

(in-ns 'your.ns)
(clojure.core/refer 'joy.ns)
joy.ns/authors

(in-ns 'joy.ns)
(def authors ["Chouser" "Fogus"])
joy.ns/authors

;; Creating namespaces

;; ns: automatically get two sets of symbolic mappings (everything in
;; java.lang, and everythin in the clojure.core namespace); creates
;; namespace if it does not exist, and switches to it; not intended
;; for the REPL

(ns chimp)
(reduce + [1 2 (Integer. 3)])

;; in-ns: automatically imports java.lang, but nothing from
;; clojure.core; also: takes an explicit symbol as namespace
;; qualifier; more amenable to REPL than ns

(in-ns 'gibbon)
(clojure.core/refer 'clojure.core)
(reduce + [1 2 (Integer. 3)])

;; create-ns: most amount of control for creating namespaces; takes a
;; symbol, returns namespace object; does not switch to the named
;; namespace; can manipulate its binding programmatically using the
;; functions `intern` and `ns-unmap`

(def b (create-ns 'bonobo))
b

((ns-map b) 'String)
(doc ns-map)

(find-ns 'bonobo)

(intern b 'x 9)
bonobo/x

(intern b 'reduce clojure.core/reduce)
(intern b '+ clojure.core/+)

(in-ns 'bonobo)
(reduce + [1 2 3 4 5])

(in-ns 'user)
(get (ns-map 'bonobo) 'reduce)
(ns-unmap 'bonobo 'reduce)
(get (ns-map 'bonobo) 'reduce)

(remove-ns 'bonobo)

(all-ns)

;; when defining namespaces, include only the references that are
;; likely to be used. :exclude, :only, :as, :refer-clojure, :import,
;; :use, :load, :require

(ns joy.ns-ex
  (:refer-clojure :exclude [defstruct]) ;; exclude defstruct macro
                                        ;; from clojure.core
  (:use (clojure set xml))              ;; pulls in too much, not recommended
  (:use [clojure.test :only (are is)])  ;; use only `are` and `is` (no
                                        ;; qualification)
  (:require (clojure [zip :as z]))      ;; alias clojure.zip namespace
                                        ;; as `z`
  (:import (java.util Date)
           (java.io File)))

                  ; clojure multimethods and the universal design pattern


(ns joy.udp
  (:refer-clojure :exclude [get]))

(defn beget [o p] (assoc o ::prototype p))
(beget {:sub 0} {:super 1})
(def put assoc)
