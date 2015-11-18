#! /bin/bash

CLASSPATH=/usr/share/java/sqlite-jdbc-3.8.11.2.jar:/usr/share/java/protobuf-java-2.5.0.jar:/usr/local/share/java/zmq.jar:/usr/share/java/java-getopt-1.0.14.jar

###### PROTO ###########
protoc proto/* --java_out=java/src/

###### JAR #############
#Make the manifest change
MF_ADDITION=.mf_addition.txt
ROOT_DIR=bin
touch $MF_ADDITION
rm $MF_ADDITION
echo "Main-Class: core.driver.Main" >> $MF_ADDITION
echo "Class-Path: ." $(echo $CLASSPATH | tr ':' ' ') >> $MF_ADDITION
#Go through all the files that should be in there:
jar cmf $MF_ADDITION searchparty.jar -C $ROOT_DIR .
rm $MF_ADDITION


####### JAVA ############
java -jar searchparty.jar --dispatcher -i ~/git/SearchPartyTest/SearchPartyTest/startup.prototxt -o not_yet_done

####### CLEAN ###########
ps -elf | grep searchparty | sed -e "s/0 S sloscal1[ ]\+\([0-9]\+\).*/\1/g" | xargs kill -9
