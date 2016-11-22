;;;; --------------------------------------------------------------------- ;;;;
;;;;                  CS 44O-01 Group Final Project                        ;;;;
;;;; --------------------------------------------------------------------- ;;;;
;;;;
;;;; Names:     Matthew Spero, Sorren Spiknall
;;;; Class:     CS440-01 (IIT Fall 2016)
;;;; Date:      November 1, 2016
;;;;
;;;; Objective: Create a Clite file verifier that will, when given a source file as input,
;;;;            produce separate output messages from the Lexer, Parser, and TYPE Checker.
;;;;            Include documentation on how to build and run the program.
;;;;
;;;;            Clite, as we refer to it, is a subset of C which supports *at least* the following:
;;;;            - data types: int, float, arrays (e.g. "float var", "int var[]")
;;;;            - typed variable declarations (e.g. "int var;")
;;;;            - boolean expressions (e.g. "true", "false && true", "true || false")
;;;;            - arithmetic expressions (e.g. "5 + 5", "var1 / var2", "var1 * 5")
;;;;            - assignment statements (e.g. "int var1 = 5;", "int var1; var1 = 5")
;;;;            - conditional statements (e.g. "if (true) { } else if (true) { } else { }")
;;;;
;;;; Notes:     This implementation makes use of the popular Clojure library Instaparse.
;;;;            Internally, Instaparse merges the concept of a lexer and parser to create
;;;;            a parse generator based on the given grammar specification, which is then
;;;;            used to create abstract syntax trees. However, its internal nodes (lexemes)
;;;;            contains both information from the lexer and parser which we use to
;;;;            produce the desired output. Since we're demonstrating functionality and not
;;;;            actually implementing the token scanner and parser ourselves, instaparse
;;;;            satisfies these requirements quite nicely.
;;;;

(ns template.core
  (:require [instaparse.core :as insta]
            [clojure.string :as str]))

(defn marker
  "Creates string with caret at nth position, 1-based"
  [n]
  (if (<= n 1) "^" (apply str (concat (repeat (dec n) \space) [\^]))))

;; A grammar specification of all tokens and terminals in CLite
(def clite-grammar-tokens
"ASSIGNMENT_OP = '=' | '+=' | '*='
 UNARY_OP = '!' | '-'
 BINARY_OP = '+' | '-' | '*' | '/'
 RELATIONAL_OP = '==' | '!=' | '<=' | '<' | '>=' | '>'
 LOGICAL_OP = '&&' | '||'

 ID = #'\\b[a-zA-Z_][a-zA-Z0-9_]*\\b'
 <IF>            = <#'\\b(if)\\b'>
 <ELSE_IF>       = <#'\\b(else)\\b'> <#'\\b(if)\\b'>
 <ELSE>          = <#'\\b(else)\\b'>
 <OPEN_BRACE>    = <'{'>
 <CLOSE_BRACE>   = <'}'>
 <OPEN_BRACKET>  = <'['>
 <CLOSE_BRACKET> = <']'>
 <OPEN_PAREN>    = <'('>
 <CLOSE_PAREN>   = <')'>
 <END_LINE>      = <';'>
 <COMMA>         = <','>

 TYPE = #'\\b(int)\\b' | #'\\b(bool)\\b' | #'\\b(float)\\b' | #'\\b(char)\\b' | #'\\b(void)\\b'
 FLOAT = #'[0-9]+.[0-9]+[f]?'
 INTEGER = #'[0-9]+'
 BOOLEAN = #'\\btrue\\b' | #'\\bfalse\\b'
 CHARACTER = <\"'\"> #'.' <\"'\">")

;; A lexer is simply a parser in which all terminal tokens are recognized in any order.
;; Thus, we use the above token specification to create a parser to simulate our lexer.
(def clite-lexer-spec
  (let [unhidden-tokens (str/replace clite-grammar-tokens #"((?<!=|')<)|>(?!'|=)" "")]
    (->> unhidden-tokens
         (str/split-lines)                                  ; split grammar into separate lines
         ((partial map (fn [x] (str/split x #"=" 2))))      ; split lines at the equal sign
         ((partial map first))                              ; get names of the terminal symbols
         (remove empty?)                                    ; remove empty entries
         (str/join "|")                                     ; "alltokens recognized in any order"
         ((fn [x] (str "TOKENS = {TOKEN}; <TOKEN> = " x " " unhidden-tokens))))))

(defn lexer
  "Returns an error string indicating what went wrong upon failure,
   and a lex 'tree' upon success."
  [source-str]
  (let [parser (insta/parser clite-lexer-spec :auto-whitespace :standard)
        parse-tree (insta/parse parser source-str)]
    (if-let [fail (insta/get-failure parse-tree)]
      (str "Unrecognized token on line " (:line fail) ", column " (:column fail) \newline
           (:text fail) \newline
           (marker (:column fail)))
      parse-tree)))

;; The parser provides a grammar specification that defines structural relations
;; between all of CLite's different tokens (i.e. its syntax). Since the tokens were
;; already defined before, the token specification is simply appended to the end of
;; the actual CLite grammar.
;;
;; [ ] -> 0 or 1
;; { } -> 1 or more
;; < > -> hiding node name or contents
(def clite-grammar-spec
  (str
    "Program = {VariableDeclaration | FunctionDeclaration | Function}
     FunctionDeclaration = TYPE ID OPEN_PAREN ParamVariables CLOSE_PAREN END_LINE

     ParamVariables = {ParamVariable}
     ParamVariable = TYPE ID [OPEN_BRACKET CLOSE_BRACKET]
                     {COMMA TYPE ID [OPEN_BRACKET CLOSE_BRACKET]}

     Declarations = {VariableDeclaration}
     VariableDeclaration = TYPE ID [OPEN_BRACKET Expr CLOSE_BRACKET]
                           {COMMA ID [OPEN_BRACKET Expr CLOSE_BRACKET]} END_LINE

     Statements = {Statement}
     <Statement> = Block | [Expr] END_LINE | Assignment END_LINE | IfStatement

     Function = TYPE ID OPEN_PAREN ParamVariables CLOSE_PAREN Block
     Block = OPEN_BRACE [Declarations] [Statements] CLOSE_BRACE

     Assignment = ID [OPEN_BRACKET Expr CLOSE_BRACKET] ASSIGNMENT_OP Expr

     IfStatement = If {ElseIf} [Else]
     If = IF OPEN_PAREN Expr CLOSE_PAREN Block
     ElseIf = ELSE_IF OPEN_PAREN Expr CLOSE_PAREN Block
     Else = ELSE Block

     <Literal> = INTEGER | CHARACTER | BOOLEAN | FLOAT
     Expr = UNARY_OP Expr |
            Expr (BINARY_OP | RELATIONAL_OP | LOGICAL_OP) Expr |
            ID [OPEN_BRACKET Expr CLOSE_BRACKET] |
            OPEN_PAREN Expr CLOSE_PAREN |
            Literal"
    " " clite-grammar-tokens))

(def tree-transformer
  {
   ;   :Expr (fn [& args] (apply concat args))
   })

(defn parser
  "Returns a string upon failure, indicating what was wrong, and
   a transformed parse tree upon success"
  [source-str]
  (let [parser (insta/parser clite-grammar-spec :auto-whitespace :standard)
        parse-tree (insta/parse parser source-str)]
    (if-let [fail (insta/get-failure parse-tree)]
      (str "Ill-formed syntax on line " (:line fail) ", column " (:column fail) \newline
           (:text fail) \newline
           (marker (:column fail)))
      (insta/transform tree-transformer parse-tree))))

(def source-file-simple
  "int main()
  {
    1 == 1;
  }")

(def source-file-complex
  "int main()
  {
    int a;
    float b;
    char c;

    a = 1 + 1;
    b = a * 5.5;
    c = '1';

    if ((true && false) || true) {
      a = 1;
    } else if ((1 * 2 == 2) && !(3 > 1)) {

    } else {

    }}
  }")

(def positive-test-case (slurp "test/positive_test.txt"))
(def negative-test-case (slurp "test/negative_test.txt"))

;(print (lexer source-file-complex))

;:output-file "ParseTree2.png" :options {:dpi 63}
;(insta/visualize)
(print (parser source-file-complex))