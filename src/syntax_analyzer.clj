(ns syntax-analyzer
  (:require [instaparse.core :as insta]
            [clite-specs]))

(def clite-syntax-parser (insta/parser clite-specs/clite-grammar-grammar :auto-whitespace :standard))

(defn parser
  "Returns a string upon failure, indicating what was wrong, and
   a transformed parse tree upon success"
  [source-str]
  (let [parse-tree (insta/parse clite-syntax-parser source-str)]
    (if-let [fail (insta/get-failure parse-tree)]
      (str "Ill-formed syntax on line " (:line fail) ", column " (:column fail) \newline
           (:text fail) \newline
           (instaparse.failure/marker (:column fail)))
      parse-tree)))

(defn expressions-transform
  ([x] x)
  ([unary-op rhs] [unary-op rhs])
  ([lhs binary-op rhs] [binary-op lhs rhs]))

(defn assignment-transform
  ([lhs op rhs] [op lhs rhs])
  ([lhs array-index op rhs] [op [:ArrayIndex lhs array-index] rhs]))

(defn variable-declaration-transform
  ([type name] [:VariableDeclaration type name])
  ([type name size] [:ArrayDeclaration type name size]))

(def parse-tree-to-ast-transform
  {
   :TYPE                (fn [type] type)
   :ID                  (fn [id] id)
   :CHARACTER           (fn [str] (.charAt str 0))
   :FLOAT               (fn [str] (Float/parseFloat str))
   :INTEGER             (fn [str] (Integer/parseInt str))
   :BOOLEAN             (fn [str] (Boolean/parseBoolean str))
   :UNARY_OP            (fn [op] op)
   :BINARY_OP           (fn [op] op)
   :ASSIGNMENT_OP       (fn [op] op)

   :VariableDeclaration variable-declaration-transform
   :Assignment          assignment-transform
   :Expr                expressions-transform
   })

(defn parse-tree->ast
  "Returns an Abstract Syntax Tree of the transformed clite parse-tree."
  [parse-tree]
  (insta/transform parse-tree-to-ast-transform parse-tree))
