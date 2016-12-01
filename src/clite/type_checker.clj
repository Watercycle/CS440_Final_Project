(ns clite.type-checker
  (:require [instaparse.core :as insta]
            [clojure.string :as str]))

(def declared-types (atom {}))
(def type-error-messages (atom []))

(defn expressions-transform
  ([x] x)
  ([unary-op rhs] rhs)
  ([lhs binary-op rhs] [:TypeComparison lhs rhs]))

(defn assignment-transform
  ([lhs op rhs] [:TypeComparison lhs rhs])
  ([lhs array-index op rhs] (do (when (not (= array-index "int"))
                                  (swap! type-error-messages conj
                                         "Array index must be of type int"))
                              [:TypeComparisonArray lhs rhs])))

(defn variable-declaration-transform
  ([type name] (do (swap! declared-types assoc name type)
                   [:VariableDeclaration type name])))

(def syntax-tree->type-tree-transform
  {

   :VariableDeclaration variable-declaration-transform
   })

(defn TypeComparison-resolver
  ([type] type)
  ([lhs-type rhs-type] (if (= lhs-type rhs-type)
                         lhs-type
                         (do (swap! type-error-messages conj
                                    (str "Can not assign " lhs-type " to " rhs-type))
                             [:FailedMatch lhs-type rhs-type]))))

(def type-check
  {
   :TypeComparison      TypeComparison-resolver
   :TypeComparisonArray (fn [lhs-type rhs-type]
                          (let [array-type (subs lhs-type 1 (- (count lhs-type) 1))]
                            (if (= array-type rhs-type)
                              rhs-type
                              (do (swap! type-error-messages conj
                                         (str "Can not assign array of type " array-type " to " rhs-type))
                                  [:FailedMatch lhs-type rhs-type]))))
   })

(defn transform1
  ([type name] [:VariableDeclaration type name])
  ([type name size & args]
   (let [array-type (str "[" type "]")]
     (if (not (vector? size))                               ; if 'size' is not an identifier
       [:VariableDeclaration array-type name]
       (into [:MultiDeclaration] (vec (map (fn [id] [:VariableDeclaration type id]) (into [name size] args))))))))

(def remove-multiline-declaration
  {
   :TYPE                (fn [type] type)
   :ID                  (fn [id] [(keyword id)])
   :CHARACTER           (fn [str] "char")
   :FLOAT               (fn [str] "float")
   :INTEGER             (fn [str] "int")
   :BOOLEAN             (fn [str] "bool")
   :UNARY_OP            (fn [op] op)
   :BINARY_OP           (fn [op] op)
   :ASSIGNMENT_OP       (fn [op] op)

   :Assignment          assignment-transform
   :Expr                expressions-transform
   :VariableDeclaration transform1
   })

(defn syntax-tree->type-tree
  ""
  [syntax-tree]
  (let [safe-tree (insta/transform remove-multiline-declaration syntax-tree)
        tree1 (insta/transform syntax-tree->type-tree-transform safe-tree)
        var-types-transform (zipmap (map first (keys @declared-types)) (map (fn [type]
                                                                  (fn [] type))
                                                                (vals @declared-types)))
        type-tree (insta/transform var-types-transform tree1)
        build-type-error-vec (insta/transform type-check type-tree)]

    (if (empty? @type-error-messages)
      type-tree
      (str/join \newline @type-error-messages))))
