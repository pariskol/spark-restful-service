#!/bin/bash


cd "$(dirname "$0")"
cd ..

git clone https://gitlab.com/Paris_Kolovos/psantasite.git
cd psantasite/
npm install
ng build --prod

cd ..
cp -r psantasite/dist/psanta/* src/main/resources
cp -r pics src/main/resources

#export JAVA_HOME=~/jdks/zulufx8

mvn clean compile assembly:single
#rm -r src/main/resources/*

main_jar=$(find ./target -name "spark*jar")
mv $main_jar target/psanta.jar
$JAVA_HOME/bin/jar -uf target/psanta.jar log4j.properties

rm -rf psantasite
rm -rf src/main/resources/pics

for file in $(git status | grep -ioh "src/main/resources/.*")
do
	rm -rf $file
done
