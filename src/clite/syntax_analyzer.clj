(ns clite.syntax-analyzer
  (:require [instaparse.core :as insta]
            [clite.specs]))

(def clite-syntax-parser (insta/parser clite.specs/clite-grammar-grammar :auto-whitespace :standard))

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
  ([type name size & args]
   (if (not (nil? (re-find #"^-?\d+\.?\d*$" (str size))))             ; if 'size' is a number
     [:VariableDeclaration [:Array type size] name]
     (into [:MultiDeclaration] (vec (map (fn [id] [:VariableDeclaration type id]) (into [name (str size)] args)))))))

(def syntax-tree->ast-transform
  {
   :TYPE                (fn [type] type)
   :ID                  (fn [id] id)
   :CHARACTER           (fn [str] (char str))
   :FLOAT               (fn [str] (read-string str))
   :INTEGER             (fn [str] (read-string str))
   :BOOLEAN             (fn [str] (read-string str))
   :UNARY_OP            (fn [op] op)
   :BINARY_OP           (fn [op] op)
   :ASSIGNMENT_OP       (fn [op] op)
   :VariableDeclaration variable-declaration-transform
   :Assignment          assignment-transform
   :Expr                expressions-transform
   })

(defn syntax-tree->ast
  "Returns an Abstract Syntax Tree of the transformed clite syntax-tree."
  [syntax-tree]
  (insta/transform syntax-tree->ast-transform syntax-tree))
