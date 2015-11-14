#! /bin/bash

CLASSPATH="bin"
for jar in `ls libs`; do
	CLASSPATH=$CLASSPATH:libs/$jar
done
CLASSPATH=$CLASSPATH:/usr/share/java/sqlite-jdbc-3.8.11.2.jar:/usr/share/java/protobuf-java-2.5.0.jar:/usr/local/share/java/zmq.jar

java -cp $CLASSPATH core.party.Dispatcher -i ~/git/SearchPartyTest/SearchPartyTest/startup.prototxt -o not_yet_done
