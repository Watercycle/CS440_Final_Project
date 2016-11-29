(ns clite.specs
  (:require [clojure.string :as str]))

;; A grammar specification of all tokens and terminals in CLite
(def clite-grammar-tokens
  "ASSIGNMENT_OP = '=' | '+=' | '*='
   UNARY_OP = '!' | '-'
   BINARY_OP = ARITHMETIC_OP | RELATIONAL_OP | LOGICAL_OP

   <ARITHMETIC_OP> = '+' | '-' | '*' | '/'
   <RELATIONAL_OP> = '==' | '!=' | '<=' | '<' | '>=' | '>'
   <LOGICAL_OP> = '&&' | '||'

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
(def clite-lexer-grammar
  (let [unhidden-tokens (str/replace clite-grammar-tokens #"((?<!=|')<)|>(?!'|=)" "")]
    (->> unhidden-tokens
         (str/split-lines)                                  ; split grammar into separate lines
         ((partial map (fn [x] (str/split x #"=" 2))))      ; split lines at the equal sign
         ((partial map first))                              ; get names of the terminal symbols
         (remove empty?)                                    ; remove empty entries
         (str/join "|")                                     ; "alltokens recognized in any order"
         ((fn [x] (str "TOKENS = {TOKEN}; <TOKEN> = " x " " unhidden-tokens))))))

;; The parser provides a grammar specification that defines structural relations
;; between all of CLite's different tokens (i.e. its syntax). Since the tokens were
;; already defined before, the token specification is simply appended to the end of
;; the actual CLite grammar.
;;
;; [ ] -> 0 or 1
;; { } -> 1 or more
;; < > -> hiding node name or contents
(def clite-grammar-grammar
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
     If = IF OPEN_PAREN Expr CLOSE_PAREN Statement
     ElseIf = ELSE_IF OPEN_PAREN Expr CLOSE_PAREN Statement
     Else = ELSE Statement

     <Literal> = INTEGER | CHARACTER | BOOLEAN | FLOAT
     Expr = UNARY_OP Expr |
            Expr BINARY_OP Expr |
            ID [OPEN_BRACKET Expr CLOSE_BRACKET] |
            OPEN_PAREN Expr CLOSE_PAREN |
            Literal"
    " " clite-grammar-tokens))