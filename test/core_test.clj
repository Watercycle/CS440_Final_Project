(ns template.core-test
  (:require [clojure.test :refer :all]
            [template.core :refer :all]))

(def positive-test-case (slurp "test/source_files/positive_test.txt"))
(def negative-test-case (slurp "test/source_files/negative_test.txt"))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))
