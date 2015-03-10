#!/bin/bash
if [ ! -e lib/anna-3.3.jar ];then 
wget -P lib/ https://mate-tools.googlecode.com/files/anna-3.3.jar
fi
if [ ! -e lib/minorthird.jar ];then
wget -P lib/ http://sourceforge.net/projects/minorthird/files/MinorThird%20Jar/minorthird-jar_20080611/minorthird_20080611.jar/download

cd lib
mv download minorthird.jar
cd ..
fi
if [ ! -e GeoNames/cities1000.zip ] ;then
wget -P GeoNames/ http://download.geonames.org/export/dump/cities1000.zip
fi
if [ ! -e GeoNames/allCountries.zip ] ;then
wget -P GeoNames/ http://download.geonames.org/export/dump/allCountries.zip
fi
if [ ! -e GeoNames/admin2Codes.txt ] ;then
wget -P GeoNames/ http://download.geonames.org/export/dump/admin2Codes.txt
fi
cd GeoNames

unzip cities1000.zip
unzip allCountries.zip  

rm cities1000.zip
rm allCountries.zip

cd ..

java -jar GeoNames/indexer.jar -index -write GeoNames/allCountries.txt GazIndex/
