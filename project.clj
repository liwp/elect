(defproject listora/elect "0.2.1-SNAPSHOT"
  :description "A Clojure library for selecting a subset of fields from composite data structures."
  :url "https://github.com/listora/elect"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/cljx"]
  :jar-exclusions [#"\.cljx$"]
  :test-paths ["target/test-classes"]

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.5.1"]
                                  [org.clojure/clojurescript "0.0-2202"]]
                   :plugins [[com.keminglabs/cljx "0.3.2"]
                             [lein-cljsbuild "1.0.3"]
                             [com.cemerick/clojurescript.test "0.3.0"]]

                   :cljx {:builds [{:source-paths ["src/cljx"]
                                    :output-path "target/classes"
                                    :rules :clj}
                                   {:source-paths ["src/cljx"]
                                    :output-path "target/classes"
                                    :rules :cljs}
                                   {:source-paths ["test/cljx"]
                                    :output-path "target/test-classes"
                                    :rules :clj}
                                   {:source-paths ["test/cljx"]
                                    :output-path "target/test-classes"
                                    :rules :cljs}]}
                   :aliases {"cleantest" ["do" "clean," "cljx" "once," "test,"
                                          "cljsbuild" "test"]
                             "deploy2" ["do" "clean," "cljx" "once," "deploy" "clojars"]}}}

  :cljsbuild {:test-commands {"node" ["node" :node-runner
                                      "this.literal_js_was_evaluated=true"
                                      "target/testable.js"]}
              :builds [{:source-paths ["target/classes" "target/test-classes"]
                        :compiler {:output-to "target/testable.js"
                                   :optimizations :advanced
                                   :pretty-print true}}]})
