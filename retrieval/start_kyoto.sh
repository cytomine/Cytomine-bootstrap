
cd config
sed -i "s/STORENAME_PARAM/KYOTOSINGLEFILE/g" ConfigServer.prop

cd /home/  && wget -q 'http://fallabs.com/kyotocabinet/pkg/kyotocabinet-1.2.76.tar.gz' -O kyotocabinet-1.2.76.tar.gz
tar -xzf kyotocabinet-1.2.76.tar.gz
rm kyotocabinet-1.2.76.tar.gz
cd kyotocabinet-1.2.76/
./configure
make
make install

cd ../  && wget -q 'http://fallabs.com/kyotocabinet/javapkg/kyotocabinet-java-1.24.tar.gz' -O kyotocabinet-java-1.24.tar.gz
tar -xzf kyotocabinet-java-1.24.tar.gz
rm kyotocabinet-java-1.24.tar.gz
cd kyotocabinet-java-1.24/

CPPFLAGS="-I/usr/lib/jvm/java-7-oracle/include/ -I/usr/lib/jvm/java-7-oracle/include/linux" ./configure
#Modifier le make en changeant bin/javac par /usr/bin/javac et idem pour javadoc, java...
sed -i "s/JAVAC = \/bin\/javac/JAVAC = \/usr\/bin\/javac/g" Makefile
sed -i "s/JAR = \/bin\/jar/JAR = \/usr\/bin\/jar/g" Makefile
sed -i "s/JAVAH = \/bin\/javah/JAVAH = \/usr\/bin\/javah/g" Makefile
sed -i "s/JAVADOC = \/bin\/javadoc/JAVADOC = \/usr\/bin\/javadoc/g" Makefile
sed -i "s/JAVARUN = \/bin\/java/JAVARUN = \/usr\/bin\/java/g" Makefile

#Modifier le make en ajoutant -I/usr/lib/jvm/java-7-oracle/include/ et -I/usr/lib/jvm/java-7-oracle/include/linux à CPPFLAGS
LINETOREPLACE=$(grep 'CPPFLAGS = ' Makefile)
LINETOREPLACE=${LINETOREPLACE//\//\\\/}
sed -i "s/$LINETOREPLACE/$LINETOREPLACE -I\/usr\/lib\/jvm\/java-7-oracle\/include\/ -I\/usr\/lib\/jvm\/java-7-oracle\/include\/linux/g" Makefile
make
make install


