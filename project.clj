(defproject clite-verifier "1.0a"
  :description "A simple program that takes a Clite source file as its arguement and will then
                perform lexical, syntactic, and (basic) type analysis on the file. Upon failure,
                a failure message will be outputted. Upon success, intermediate tree structures
                will be sent to the 'output' folder."

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [instaparse "1.4.3"]
                 [rhizome "0.2.7"]]

  :aot :all
  :main clite.core)