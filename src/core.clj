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
            [lexer :refer [lexer]]
            [syntax-analyzer :refer [parser parse-tree->ast]]))

; TODO: prompt for source file to verify when progam is ran
;(defn -main
;  "This should be pretty simple."
;  []
;  (println "Hello, World!"))

(def source-file-complex
  "int main()
  {
    int a;
    float b[3];
    char c;

    a = 1 + 1;
    b[0] = a * 5.5;
    c = '1';

    if ((true && false) || true) {
      a = 1;
    } else if ((1 * 2 == 2) && !(3 > 1)) {

    } else {

    }
  }")

;:output-file "ParseTree2.png" :options {:dpi 63}
(insta/visualize (parse-tree->ast (parser source-file-complex)))