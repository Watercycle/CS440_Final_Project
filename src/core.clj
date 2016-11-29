;;;; --------------------------------------------------------------------- ;;;;
;;;;                  CS 44O-01 Group Final Project                        ;;;;
;;;; --------------------------------------------------------------------- ;;;;
;;;;
;;;; Names:     Matthew Spero, Sorren Spiknall
;;;; Class:     CS440-01 (llinois Tech Fall 2016)
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
            [syntax-analyzer :refer [parser syntax-tree->ast]]
            [type-checker :refer [ast->type-tree]]))

 ;TODO: prompt for source file to verify when progam is ran
;(defn -main
;  "This should be pretty simple."
;  []
;  (println "Hello, World!"))

(def source-file-complex (slurp "test/source_files/comprehensive_test.txt"))

(defn clite-verifier
  "Prints error message or outputs all of the completed trees."
  [source-str]
  (let [lex-tree (lexer source-str)
        syntax-tree (parser source-str)
        ast-tree (syntax-tree->ast syntax-tree)
        type-tree (ast->type-tree ast-tree)]

    (cond (instance? String lex-tree) (println lex-tree)
          (instance? String syntax-tree) (println syntax-tree)

          :success

          (do (println "Successfully parsed source-str. Generating intermediate trees...")
              (.mkdir (java.io.File. "output"))
              (insta/visualize lex-tree :output-file "output/TokenStreamOutput.png")
              (insta/visualize syntax-tree :output-file "output/SyntaxTreeOutput.png")
              (insta/visualize ast-tree :output-file "output/ASTOutput.png")
              (println "Finished creating intermediate trees. See output folder.")))))

(clite-verifier source-file-complex)
