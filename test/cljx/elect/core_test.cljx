(ns elect.core-test
  (:require [clojure.test :refer :all]
            [elect.core :refer :all]))

(deftest elect-pre-conditions
  (testing "elect"
    (testing "verifies `paths` is a vector"
      (are [paths] (thrown? AssertionError (elect {} paths))
           1
           "foo"
           :foo
           'foo
           {:foo :bar}
           #{:foo}
           '(:foo)))))

(deftest elect-primitives
  (testing "elect"
    (testing "with primitives returns primitive"
      (are [x] (= (elect x [:foo]) x)
           1
           "foo"
           :foo
           'foo))))

(deftest elect-maps
  (testing "elect"
    (testing "with single-level maps"
      (let [obj {:foo "foo"
                 :bar [1 2 3]}]
        (are [paths res] (= (elect obj paths) res)
             [:non-existent-path] {}
             [:foo :bar] obj
             [:foo] {:foo "foo"}
             [:bar] {:bar [1 2 3]})))

    (testing "with two-level maps"
      (let [obj {:a {:foo "foo"
                     :bar [1 2 3]}}]
        (are [paths res] (= (elect obj paths) res)
             [{:a [:non-existent-path]}] {:a {}}
             [{:a [:foo]}] {:a {:foo "foo"}}
             [{:a [:foo :bar]}] obj
             [:non-existent-path] {})))))

(deftest elect-vectors
  (testing "elect"
    (testing "with top-level vector"
      (testing "with primitives"
        (let [obj [1 "foo" :bar]]
          (is (= (elect obj [:foo]) obj))))

      (testing "with maps"
        (let [obj [{:foo 1} {:foo "foo"} {:foo :bar}]]
          (are [paths res] (= (elect obj paths) res)
               [:foo] obj
               [:bar] [{} {} {}])))

      (testing "with nested vectors"
        (let [obj [[1] [2] [3]]]
          (is (= (elect obj [:foo]) obj)))
        (let [obj [[{:foo 1}] [{:foo "foo"}] [{:foo :bar}]]]
          (is (= (elect obj [:foo]) obj))
          (is (= (elect obj [:bar]) [[{}] [{}] [{}]])))))))

(deftest elect-large-object
  (testing "elect paths from a large, nested object"
    (let [input-obj
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
                     :formatted-address "The Castle Pub, Cambridge, UK"}}]}

          paths
          [:name
           :summary
           :description
           :tags
           {:occurrences [:start-time
                          :end-time
                          {:place [:name
                                   :formatted-address]}]}]

          expected-output-obj
          {:name "Cambridge Clojure user group"
           :summary "The Cambridge Clojure user group meets once a month."
           :description "A monthly meetup for anyone with interest in Clojure."
           :tags ["clojure", "programming", "user group"]
           :occurrences
           [{:start-time "2014-05-22T20:00:00Z"
             :end-time "2014-05-22T22:00:00Z"
             :place {:name "The Bridge Pub"
                     :formatted-address "The Bridge Pub, Cambridge, UK"}}
            {:start-time "2014-06-22T20:00:00Z"
             :end-time "2014-06-22T22:00:00Z"
             :place {:name "The Bridge Pub"
                     :formatted-address "The Bridge Pub, Cambridge, UK"}}
            {:start-time "2014-07-22T20:00:00Z"
             :end-time "2014-07-22T22:00:00Z"
             :place {:name "The Castle Pub"
                     :formatted-address "The Castle Pub, Cambridge, UK"}}]}]
      (is (= (elect input-obj paths) expected-output-obj)))))
