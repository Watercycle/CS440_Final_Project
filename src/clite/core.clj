;;;; --------------------------------------------------------------------- ;;;;
;;;;                  CS 44O-01 Group Final Project                        ;;;;
;;;; --------------------------------------------------------------------- ;;;;
;;;;
;;;; Names:     Matthew Spero, Sorren Spiknall, Neil Okhandiar
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

(ns clite.core
  (:gen-class :main true)
  (:require [instaparse.core :as insta]
            [clite.lexer :refer [lexer]]
            [clite.syntax-analyzer :refer [parser syntax-tree->ast]]
            [clite.type-checker :refer [syntax-tree->type-tree]]
            [clojure.walk :as walk]))

(defn clite-verifier
  "Prints error message or outputs all of the completed trees."
  [source-str]
  (let [lex-tree (lexer source-str)
        syntax-tree (parser source-str)
        ast-tree (syntax-tree->ast syntax-tree)
        type-tree (syntax-tree->type-tree syntax-tree)]

    (cond (instance? String lex-tree) (println lex-tree)
          (instance? String syntax-tree) (println syntax-tree)
          (instance? String type-tree) (println type-tree)

          :success
          (do (println "Successfully parsed source-str. Generating intermediate trees...")
              (.mkdir (java.io.File. "output"))
              (insta/visualize lex-tree :output-file "output/1_TokenStreamOutput.png")
              (insta/visualize syntax-tree :output-file "output/2_SyntaxTreeOutput.png")
              (insta/visualize ast-tree :output-file "output/3_ASTOutput.png")
              (insta/visualize type-tree :output-file "output/4_TypeTree.png")
              (println "Finished creating intermediate trees. See output folder.")))))

(defn -main
  "This should be pretty simple."
  [& args]
  (clite-verifier (slurp (first args)))
  (System/exit 0))
