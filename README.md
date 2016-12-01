# CLite File Verifier

A simple executable program that takes a CLite source file as its argument and will then 
perform lexical, syntactic, and (basic) type analysis on the file. Upon failure, 
a failure message will be outputted. Upon success, intermediate tree structures 
will be sent to the 'output' folder. These trees represent the underlying transformations
that take place while parsing source files.

## Dependencies

Since this program uses Clojure, make sure you have the
[latest Java Runtime](https://www.java.com/en/) from Oracle

Instaparse depends on Graphviz to draw out the parse trees.
It may be installed from [their website](http://www.graphviz.org/Download.php)

_Note:_
Windows users, make sure to you create a new entry for the graphviz 
bin folder in your system environment variables path.

## Usage

You may run the distributed version with the following command.

```shell
java -jar cverify.jar SOURCE_FILE
```

Where SOURCE_FILE is the qualified file directory and name to be 'verified'.

## Demonstration

Suppose relative to the executable we have a folder named 'tests', 
which contains the following file.

**comprehensive_test.txt**
```c
int main()
{
    int a,b,c,d;
    float e,f,g,h;
    int i[3];
    
    a =  + ;
}
```

We may verify the file as follows:

```shell
java -jar cverify.jar tests/comprehensive_test.txt
```

Which produces the following error output:

![alt text](https://github.com/Watercycle/CS440_Final_Project/blob/master/doc/failure.png "Failure")

Had the file been properly formed, we would have been given its parse trees in the 'output' folder,
which would looking something like the following:

![alt text](https://github.com/Watercycle/CS440_Final_Project/blob/master/doc/success.png "Success")

## Building

To build the program, simply navigate to the project directory and run the following:

```shell
lein uberjar
```
(http://leiningen.org/)

A standalone jar file will be available in the 'target' folder.