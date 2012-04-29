(ns joc.ch11)

(doc io!)

(io! (.println System/out "Haikeeba!"))

(dosync (.println System/out "Haikeeba!"))

;; java.lang.IllegalStateException (I/O in transaction)
;; (dosync (io! (.println System/out "Haikeeba!")))




