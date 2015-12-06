#! /bin/bash

###### OVERALL VARIABLES #
JAR_DIR=/home/sloscal1/jars
CLASSPATH=$JAR_DIR/sqlite-jdbc-3.8.11.2.jar:$JAR_DIR/protobuf-java-2.5.0.jar:$JAR_DIR/jeromq-0.3.6-SNAPSHOT.jar:$JAR_DIR/java-getopt-1.0.14.jar
JAVA_CLASS_DIR=bin
JAVA_SRC_DIR=java/src
PROTO_DIR=proto

###### PROTO #############
printf "Compiling protobuf messages...\n"
if [ -d $JAVA_SRC_DIR/core/messages ]; then
  rm $JAVA_SRC_DIR/core/messages/*
fi
protoc $PROTO_DIR/* --java_out=$JAVA_SRC_DIR

###### JAVA COMPILE ######
printf "Compiling java source files...\n"
JAVA_SRCS=.javafiles
touch $JAVA_SRCS
rm $JAVA_SRCS
find $JAVA_SRC_DIR -type f -name *.java >> $JAVA_SRCS
rm -rf $JAVA_CLASS_DIR
mkdir $JAVA_CLASS_DIR
javac -cp $CLASSPATH -d $JAVA_CLASS_DIR @$JAVA_SRCS
rm $JAVA_SRCS

###### JAR #############
printf "Building searchparty.jar file...\n"
#Make the manifest change
MF_ADDITION=.mf_addition.txt
touch $MF_ADDITION
rm $MF_ADDITION
echo "Main-Class: core.driver.Main" >> $MF_ADDITION
echo "Class-Path: ." $(echo $CLASSPATH | tr ':' ' ') >> $MF_ADDITION
#Go through all the files that should be in there:
jar cmf $MF_ADDITION searchparty.jar -C $JAVA_CLASS_DIR .
rm $MF_ADDITION

if [ "$1" == "" ]; then
  ####### JAVA ############
  printf "Executing experiment...\n"
  java -jar searchparty.jar --dispatcher -i ~/git/SearchPartyTest/SearchPartyTest/startup.prototxt -o not_yet_done
  
  ####### CLEAN ###########
  printf "Stopping %d zombie process(es)...\n" $(let "$(ps -elf | grep searchparty | wc -l) - 1")
  ps -elf | grep searchparty | sed -e "s/0 S sloscal1[ ]\+\([0-9]\+\).*/\1/g" | xargs kill -9
fi
