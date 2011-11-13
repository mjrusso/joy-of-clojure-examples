(ns joc.ch3)

; all truthy
(if true :truthy :falsey)
(if [] :truthy :falsey)
(if 0 :truthy :falsey)

; all falsey (only nil and false are actually false)
; (every object is "true" all the time, unless its nil or false)
(if nil :truthy :falsey)
(if false :truthy :falsey)




