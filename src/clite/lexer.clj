(ns clite.lexer
  (:require [instaparse.core :as insta]
            [clite.specs]))

(def clite-lex-parser (insta/parser clite.specs/clite-lexer-grammar :auto-whitespace :standard))

(defn lexer
  "Returns an error string indicating what went wrong upon failure,
   and a lex 'tree' upon success."
  [source-str]
  (let [parse-tree (insta/parse clite-lex-parser source-str)]
    (if-let [fail (insta/get-failure parse-tree)]
      (str "Unrecognized token on line " (:line fail) ", column " (:column fail) \newline
           (:text fail) \newline
           (instaparse.failure/marker (:column fail)))
      parse-tree)))
