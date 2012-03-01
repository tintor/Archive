(def (not a) (if a false true))

(def (!= a b) (not (= a b)))
(def (>= a b) (not (< a b)))
(def (<= a b) (not (> a b)))

(def (size (: a x)) (+ (size x) 1)))
(def (size ()) 0)

(def (get (: a x) n) (get x (-n 1)))
(def (get (: a x) 0) a)

(def (empty? (: a x)) false)
(def (empty? ()) true)

(def (map f (: a x)) (: (f a) (map f x)))
(def (map f ()) ())

(def (select f (: a x)) (if (f a) (: a (select f x)) (select f x)))
(def (select f ()) ())

(def (reduce f i (: a x)) (reduce f (f i a) x))
(def (reduce f i ()) i)

(def (any? f (: a x)) (if (f a) true (any? f x)))
(def (any? f ()) false)

(def (all? f (: a x)) (if (f a) (all? f x) false))
(def (all? f ()) true)

(def (sqr x) (* x x))
(def (map-sqr x) (map sqr x))

(def (concat (: a x) b) (: a (concat x b)))
(def (concat () b) b)
(def (concat a ()) a)

(def (> (: a _) (: b _)) (> a b))
(def (> (: a x) (: a y)) (> x y))
(def (> (: _ _) ()) true)
(def (> () _) false)

(def (integers i) (: i (integers (+ i 1))))

(def (assert false) (error 'assertion))
(def (assert true) ())
(def (assert= _ _) (error 'assertion))
(def (assert= a a) ())

(assert= '(1 2 3 4) (: 1 '(2 3 4)))

(def (qsort (: a x)) (concat (select (func (i) (< i a)) x) (: a (select (func (i) (>= i a)) x)))
(def (qsort ()) ())
(assert= '(0 1 2 3 5 6 7 7) (qsort '(2 7 6 3 0 1 5 7)))

(def (add-1 x) (+ x 1))
(def add-1 (func (x) (+ x 1)))

(def (range s e) (: s (range (+ s 1) e)))
(def (range s s) ())

(def sum (reduce +))
(assert= 55 (sum (range 1 11)))