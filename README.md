# Elect

[![Build Status](https://travis-ci.org/listora/elect.png?branch=master)](https://travis-ci.org/listora/elect)

A Clojure library for selecting a subset of fields from composite data
structures (ie nested maps and vectors).

Clojars: `[listora/elect "0.1.0"]`

--

Often an application gets objects (eg JSON converted to maps) from
somewhere where only a subset of the fields in that object are
actually interesting. For example, we might want to prune out any
unknown fields from an HTTP POST request body before writing it to a
database. For flat maps this is trivial to do with `select-keys`, but
for nested maps and vectors the pruning becomes quite tedious. `elect`
is here to help!

## Usage

The `elect` API consists of only the `elect.core/elect` function.

```clojure
(require '[elect.core :as e])
```

The `elect` function takes two arguments: an object to prune and a
path specification describing which paths to retain.

Only maps and vectors are pruned by `elect`. Other types are passed
through as they are:

```clojure
(e/elect 1 [:foo]) ;=> 1
(e/elect :bar [:foo]) ;=> :bar
(e/elect "baz" [:foo]) ;=> "baz"
```

Only those fields of an input *map* are retained that are specified in
the path specification:

```clojure
(e/elect {:foo 1 :bar 2} [:foo]) ;=> {:foo 1}
(e/elect {:foo 1 :bar 2} [:foo :bar]) ;=> {:foo 1 :bar 2}
(e/elect {:foo 1 :bar 2} [:baz]) ;=> {}
```

Fields of nested maps are addressed by maps in the path specification.
The key specifies the top level object key and the value is a vector
specifying keys for the nested map:

```clojure
(e/elect {:foo {:bar 1} :baz {:quux 2}} [{:foo [:bar]]}]) ;=> {:foo {:bar 1}}
```

Nesting is supported arbitrarily deep, but the implementation is not
tail-recursive so eventually you will run out of stack. On the other
hand, if your objects are that heavily nested, maybe you have other
problems ;)

Unmatching paths will maintain the shape of the object:

```clojure
(e/elect {:foo {:quux 1}} [{:foo [:bar]}]) ;=> {:foo {}}
```

Vectors in an object are treated transparently as collections of
objects, ie the path spec is applied to each item in the vector:

```clojure
(e/elect [{:foo 1} {:foo 2}] [:foo]) ;=> [{:foo 1} {:foo 2}]
(e/elect [1 2 3] [:foo]) ;=> [1 2 3]
(e/elect {:foo [{:bar 1} {:bar 2}]} [{:foo [:bar]}]) ;=> {:foo [{:bar 1} {:bar 2}]}
```

Finally, here's a larger example. We have a piece of data and we want
to retain only a subset of the fields as specified by `paths`:

```clojure
(require '[elect.core :as e]
         '[clojure.pprint :refer [pprint]])

(def paths
  [:name
   :summary
   :description
   :tags
   {:occurrences [:start-time
                  :end-time
                  {:place [:name
                           :formatted-address]}]}])

(def input
  {:id "90f400a49c5440b3934ab5ac18a22a97"
   :created-at "2014-05-01T11:20:22Z"
   :updated-at "2014-05-01T11:21:22Z"
   :name "Cambridge Clojure user group"
   :summary "The Cambridge Clojure user group meets once a month."
   :description "A monthly meetup for anyone with interest in Clojure."
   :tags ["clojure", "programming", "user group"]
   :occurrences
   [{:id "8c8d240f15dd47bf89418d0e4ec97473"
     :created-at "2014-05-01T11:40:18Z"
     :updated-at "2014-05-01T12:40:18Z"
     :start-time "2014-05-22T20:00:00Z"
     :end-time "2014-05-22T22:00:00Z"
     :place {:id "30885f98ec454ce4bf138c218b41910f"
             :name "The Bridge Pub"
             :formatted-address "The Bridge Pub, Cambridge, UK"}}
    {:id "04650f03398d4c5b86fd8eaee2d65293"
     :created-at "2014-05-01T11:40:18Z"
     :updated-at "2014-05-01T12:40:18Z"
     :start-time "2014-06-22T20:00:00Z"
     :end-time "2014-06-22T22:00:00Z"
     :place {:id "30885f98ec454ce4bf138c218b41910f"
             :name "The Bridge Pub"
             :formatted-address "The Bridge Pub, Cambridge, UK"}}
    {:id "fc1956d18ad24b1895951e9cb7860004"
     :created-at "2014-05-01T11:40:18Z"
     :updated-at "2014-05-01T12:40:18Z"
     :start-time "2014-07-22T20:00:00Z"
     :end-time "2014-07-22T22:00:00Z"
     :place {:id "36807b7b317a4dd289320fb3cb882dba"
             :name "The Castle Pub"
             :formatted-address "The Castle Pub, Cambridge, UK"}}]})

(pprint (e/elect input paths))

(comment
  {:name "Cambridge Clojure user group",
   :summary "The Cambridge Clojure user group meets once a month.",
   :description "A monthly meetup for anyone with interest in Clojure.",
   :tags ["clojure" "programming" "user group"],
   :occurrences
   [{:start-time "2014-05-22T20:00:00Z",
     :end-time "2014-05-22T22:00:00Z",
     :place
     {:name "The Bridge Pub",
      :formatted-address "The Bridge Pub, Cambridge, UK"}}
    {:start-time "2014-06-22T20:00:00Z",
     :end-time "2014-06-22T22:00:00Z",
     :place
     {:name "The Bridge Pub",
      :formatted-address "The Bridge Pub, Cambridge, UK"}}
    {:start-time "2014-07-22T20:00:00Z",
     :end-time "2014-07-22T22:00:00Z",
     :place
     {:name "The Castle Pub",
      :formatted-address "The Castle Pub, Cambridge, UK"}}]}
)
```

## Limitations

The library is very young, so for the time being it's really
addressing the immediate needs that Listora has. Hence there are
certain limitations in the current implementation:

1. As I said above, the implementation is not tail-recursive and will
consume stack. I don't see this changing since I believe it to be
extremely rare for users to have objects that are so heavily nested as
to run out of stack

2. Only keywords are supported as map keys. There is no technical
reason not to support other types of keys, I just simply haven't had
time to try it out yet. It should be trivial to extend the `Path`
protocol to other types, or simply extend it to
`java.lang.Object`. There is a technical reason why maps cannot be
used as map keys, but that seems like a reasonable restriction.


## Inspiration

The library was inspired by this
[thread](https://groups.google.com/forum/#!topic/clojure/gGigc9BWzNw)
on the Clojure mailing list and of course by the need to scratch our
own itch. The path syntax is taken verbatim from
[@ghoseb's](https://github.com/ghoseb) original post. And the
implementation is heavily influenced by
[@cgrand's](https://github.com/cgrand) beautiful
[gist](https://gist.github.com/cgrand/2823916).


## License

Copyright © 2014 Listora

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
