# CLite File Verifier

A simple program that takes a Clite source file as its arguement and will then 
perform lexical, syntactic, and (basic) type analysis on the file. Upon failure, 
a failure message will be outputted. Upon success, intermediate tree structures 
will be sent to the 'output' folder."

## Usage

The distributed version is ran with a simple command line

```shell
java -jar cverify.jar SOURCE_FILE
```

Where SOURCE_FILE is the qualified file directory and name to be used.

For example, if there is a folder named 'tests' in thesame directory as the jar file,
java -jar cverify.jar tests/comprehensive_test.txt


## Building

Navigate to the project directory and run 

```shell
lein uberjar
```
(http://leiningen.org/)

A standalone jar file will be available under the 'target' directory.

## Demonstration

For example, the following code

```c
int main()
{
    int a,b,c,d;
    float e,f,g,h;
    int i[3];
    
    a =  + ;
}
```

produces the following error:

![alt text](https://github.com/Watercycle/CS440_Final_Project/blob/master/doc/failure.png "Failure")

![alt text](https://github.com/Watercycle/CS440_Final_Project/blob/master/doc/success.png "Success")

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
