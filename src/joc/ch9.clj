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

                  ; clojure multimethods and the universal design
                  ; pattern (UDP)


(ns joy.udp
  (:refer-clojure :exclude [get]))

(defn beget [o p] (assoc o ::prototype p))
(beget {:sub 0} {:super 1})

(def put assoc)

;; follows the prototype chain until the end
(defn get [m k]
  (when m
    (if-let [[_ v] (find m k)]
      v
      (recur (::prototype m) k))))

;; find allows us to distinguish between 'nil' and 'not found'
(doc find)
(find {:a 1, :c 3} :a)
(:a {:a 1, :c 3})
(:z {:a 1, :c 3})
(find {:a 1, :c 3} :z)
(find {:a nil, :c 3} :a)

(get (beget {:sub 0} {:super 1})
     :super)

(def cat {:likes-dogs true, :ocd-bathing true})
(def morris (beget {:likes-9lives true} cat))
(def post-traumatic-morris (beget {:likes-dogs nil} morris))

(get cat :likes-dogs)
(get morris :likes-dogs)
(get post-traumatic-morris :likes-dogs)

;; bring on the multimethods...

;; define a multimethod `compiler` that dispatches on key :os
(defmulti compiler :os)
(defmethod compiler ::unix [m] (get m :c-compiler))
(defmethod compiler ::osx  [m] (get m :c-compiler))

;; if the function compieler is called with a prototype map, then the
;; map is queried for an element :os, which has methods defined on
;; the results for either ::unix or ::osx

(def clone (partial beget {}))
(def unix {:os ::unix, :c-compiler "cc", :home "/home", :dev "/dev"})
(def osx (-> (clone unix)
             (put :os ::osx)
             (put :c-compiler "gcc")
             (put :home "/Users")))

(compiler unix)
(compiler osx)

(defmulti home :os)
(defmethod home ::unix [m] (get m :home))

(home unix)
;; (home osx)

;; "::osx is a ::unix"
(derive ::osx ::unix)

(home osx)

(parents ::osx)
(ancestors ::osx)
(descendants ::unix)
(ancestors ::unix)
(isa? ::osx ::unix)
(isa? ::unix ::osx)

(derive ::osx ::bsd)
(defmethod home ::bsd [m] "/home")

;; (home osx)

(prefer-method home ::unix ::bsd)
(home osx)

(remove-method home ::bsd)
(home osx)

(derive (make-hierarchy) ::osx ::unix)

;; sidebar: the 'juxt' function
;; juxt takes a number of functions and composes them into a function
;; returning a vector of its argument(s) applied to each given
;; function
(def each-math (juxt + * - /))
(each-math 2 3)
((juxt take drop) 3 (range 9))

;; juxt is very useful for defining multimethod dispatch functions

;; multimethods are fully open and can dispatch on the results of an
;; arbitrary function

(defmulti compile-cmd (juxt :os compiler))

(defmethod compile-cmd [::osx "gcc"] [m]
  (str "/usr/bin/" (get m :c-compiler)))

(defmethod compile-cmd :default [m]
  (str "Unsure where to locate " (get m :c-compiler)))

(compile-cmd osx)

((juxt :os compiler) osx)

(compile-cmd unix)

;; clojure also provides a simpler model of rcreating abstractions and
;; gaining the benefits of polymorphism: types, protocols, records.
;; multimethods not always ideal: speed, and dispatching on an
;; arbitrary function is often overkill.

;; TODO return back to this chapter; currently on page 189


