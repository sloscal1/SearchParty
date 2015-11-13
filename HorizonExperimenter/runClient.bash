#! /bin/bash

CLASSPATH="bin"
for jar in `ls libs`; do
	CLASSPATH=$CLASSPATH:libs/$jar
done
CLASSPATH=$CLASSPATH:/usr/share/java/sqlite-jdbc-3.8.11.2.jar:/usr/share/java/protobuf-java-2.5.0.jar:/usr/local/share/java/zmq.jar

export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib
echo $CLASSPATH
java -cp $CLASSPATH core.party.Searcher -c $1 &> /home/sloscal1/git/HorizonExperimenter/HorizonExperimenter/temp.text
