(ns elect.core)

(declare elect)
(declare map-kv)

(defprotocol Electee
  "An electee is an object we want to elect paths from."
  (select-paths [obj paths] "Extract the given `paths` from `obj`."))

(defprotocol Path
  "A path specifies which parts of an object should be elected."
  (lookup-path [path obj] "Lookup the field identified by `path` from `obj`."))

(defn- select-paths-for-map
  [m paths]
  (->> paths
       (map #(lookup-path % m))
       (into {})))

(extend-protocol Electee
  #+clj clojure.lang.IPersistentMap
  #+cljs PersistentArrayMap
  (select-paths [m paths]
    (select-paths-for-map m paths))

  #+cljs PersistentHashMap
  #+cljs (select-paths [m paths]
           (select-paths-for-map m paths))

  #+cljs PersistentTreeMap
  #+cljs (select-paths [m paths]
           (select-paths-for-map m paths))

  #+clj clojure.lang.IPersistentVector
  #+cljs PersistentVector
  (select-paths [v paths]
    (vec (map #(elect % paths) v))))

(defn- lookup-paths-for-map
  [paths m]
  (map-kv #(elect (get m %1) %2) paths))

(extend-protocol Path
  #+clj clojure.lang.Keyword
  #+cljs Keyword
  (lookup-path [path obj]
    (find obj path))

  #+clj clojure.lang.IPersistentMap
  #+cljs PersistentArrayMap
  (lookup-path [paths m]
    (lookup-paths-for-map paths m))

  #+cljs PersistentHashMap
  #+cljs (lookup-path [paths m]
           (lookup-paths-for-map paths m))

  #+cljs PersistentTreeMap
  #+cljs (lookup-path [paths m]
           (lookup-paths-for-map paths m)))

(defn- map-kv
  "Map `f` to each element in the map `m` returning a new map. `f` is a two
  argument function where the first argument is the key of the map element
  and the second argument is the value of the map element. The elements in
  the new map will be keyed with the original keys, but the values will the
  result of applying `f` to the element key and value. Eg
  (map-kv vector {:a 1}) => {:a [:a 1]}"
  [f m]
  (reduce-kv (fn [out-m k v] (assoc out-m k (f k v)))
             {}
             m))

(defn elect
  "Select fields identified by `paths` from `obj`. `obj` can be of any type,
  but paths are applied only to maps and vectors. `paths` must be a
  vector of either keywords or maps of vectors (see the examples below).

  (elect 1 [:foo]) ;=> 1
  (elect {:foo 1} [:foo]) ;=> {:foo 1}
  (elect [{:foo 1} {:foo 2}] [:foo]) ;=> [{:foo 1} {:foo 2}]
  (elect {:foo 1} [:bar]) ;=> {}
  (elect {:foo {:bar 1}} [{:foo [:bar]}]) ;=> {:foo {:bar 1}}
  (elect {:foo {:bar 1}} [{:foo [:baz]}]) ;=> {:foo {}}
  (elect {:foo [{:bar 1} {:bar 2}]} [{:foo [:bar]}]) ;=> {:foo [{:bar 1} {:bar 2}]}"
  [obj paths]
  {:pre [(vector? paths)]}
  (if ((some-fn map? vector?) obj)
    (select-paths obj paths)
    obj))
