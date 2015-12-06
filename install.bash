#! /bin/bash

LOCAL_JAR_DIR=/home/sloscal1/Programming/jars
PREFIX_DIR=$(pwd)/.install
DEP_DIR=deps

#Needed to compile libzmq with libsodium
#export PKG_CONFIG_PATH=$PKG_CONFIG_PATH:$PREFIX_DIR/lib/pkgconfig
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$PREFIX_DIR/lib

if [ ! -d $PREFIX_DIR ]; then
  mkdir -p $PREFIX_DIR;
fi

#NEED these dependencies
echo "Install: sudo apt-get install libtool pkg-config build-essential autoconf automake maven"

echo "Moving into dependency folder $DEP_DIR"
if [ ! -d $DEP_DIR ]; then
	mkdir -p $DEP_DIR
fi
cd $DEP_DIR;

echo "Getting sqlite..."
wget https://bitbucket.org/xerial/sqlite-jdbc/downloads/sqlite-jdbc-3.8.11.2.jar
mv sqlite-jdbc-3.8.11.2.jar $LOCAL_JAR_DIR/

#echo "Getting ZMQ..."
#git clone git://github.com/jedisct1/libsodium.git
#cd libsodium
#./autogen.sh
#./configure --prefix=$PREFIX_DIR
#make -j8
#make install
#cd ..
#
#wget http://download.zeromq.org/zeromq-4.1.3.tar.gz
#tar xf zeromq-4.1.3.tar.gz
#cd zeromq-4.1.3
#sed -e "s|\(libzmq_werror\)=.*|\1=\"no\"|" -i configure.ac
#./autogen.sh
#./configure --prefix=$PREFIX_DIR --disable-werror
#make -j8
#make install
#cd ..
#
#echo "Getting JZMQ..."
#git clone https://github.com/zeromq/jzmq.git
#cd jzmq
#./autogen.sh
#./configure --prefix=$PREFIX_DIR
#make -j8
#make install
#cd ..
#
git clone https://github.com/zeromq/jeromq.git
cd jeromq
mvn package
cp target/jeromq-0.3.6-SNAPSHOT.jar $LOCAL_JAR_DIR/

echo "Getting Java getopt..."
wget http://www.urbanophile.com/arenn/hacking/getopt/java-getopt-1.0.14.jar
mv java-getopt-1.0.14.jar $LOCAL_JAR_DIR

echo "Need Google Protocol Buffer Compiler (protoc)?"
echo "Getting protoc..."
git clone https://github.com/google/protobuf.git
cd protobuf
git checkout v2.6.1 .
./autogen.sh
./configure --prefix=$PREFIX_DIR
make -j8
make install

cd ..
wget http://mirrors.ibiblio.org/maven2/com/google/protobuf/protobuf-java/2.6.1/protobuf-java-2.6.1.jar
mv protobuf-java-2.6.1.jar $LOCAL_JAR_DIR

#This should work for mvn (incomplete due to work proxy issues)	
#	cd java
#	mvn package
cd ../..

echo "Copy the contents of $PREFIX_DIR to /usr/local with sudo for best results..."
echo "Your CLASSPATH must contain the contents of $PREFIX_DIR/share/java and $LOCAL_PREFIX_DIR"
echo "Your LD_LIBRARY_PATH must contain $PREFIX_DIR/lib"
echo "Your PATH should contain $PREFIX_DIR/bin"
