# JLQN
Java GUI for layered queueing network specification

## Getting started
You can launch the GUI using:
```
java -jar target/jlqn-singlejar.jar
``` 
Examples models are available under the *examples/* folder.

It is also possible to open directly a model using:
```
java -jar target/jlqn-singlejar.jar examples/example.jlqn
``` 
The JLQN GUI is shipped with the LN solver (Java version) of [LINE](https://line-solver.sourceforge.net/). JLQN also supports the [LQNS solver](https://www.sce.carleton.ca/rads/lqns/) if installed on the system. 

## Model assumptions
This version of JLQN takes the following assumptions: 
* All demands are exponentially distributed with the specified mean.
* No forwarding calls.
* A single replyTo activity for each entry.
