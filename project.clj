(defproject mfp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.10.439"]]
  :plugins [[lein-cljsbuild "1.1.7"]]
  :source-paths ["src/clj"]
  :cljsbuild
  {:builds
   [{:compiler
     {:output-to "resources/public/mfp.js",
      :optimizations :whitespace,
      :pretty-print true},
     :source-paths ["src/cljs"]}]})

