(defproject listora/elect "0.1.1-SNAPSHOT"
  :description "A Clojure library for selecting a subset of fields from composite data structures."
  :url "https://github.com/listora/elect"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.5.1"]]
                   :plugins [[com.keminglabs/cljx "0.3.2"]]
                   :hooks [cljx.hooks]
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
                                    :rules :cljs}]}}}

  :jar-exclusions [#"\.cljx$"])
