# Project Title

Cluster Auto-Healer using Zookeeper

## Description

in order to keep a distribued system or app running correctly with load balancing, certain characteristics must be acheived.
we present simple idea of keeping a fixed number of wrokers in the cluster by watching the cluster by running master using zookeeper
so when a worker fails, we launch a new worker.
wrokers join the cluster register as znode in zookeeper and master keeps an eye on them.
workers here are simplem, just printing some lines on conole.


## Getting Started
### Dependencies

* zookeeper-3.9.1-dependency
* zookeeper server running in a machine (Virtual Machine or container) with identical verison of its dependency

### Installing

* change zookeeper connection info (IP + Port) as the connection to the pre-mentioned server.

### Executing program

java -Dorg.slf4j.simpleLogger.defaultLogLevel=off -jar .\AutoHealer.jar <number of desired workers> ".\Worker.jar"
or
java -jar .\AutoHealer.jar <number of desired workers> ".\Worker.jar"


## Authors

Contributors names and contact info

* Name: Mohammed Salameh
* email : mohammedsalameh37693@gmail.com
* LinkedIn : www.linkedin.com/in/mohammed-salameh-8b4811313
