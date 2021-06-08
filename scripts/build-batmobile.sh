#!/bin/bash


cd "$(dirname "$0")"
cd ..

cp -r ~/js-workspace/batmobile-site src/main/resources/static
mvn clean compile assembly:single
rm -rf src/main/resources/static
mv target/spark-rest-0.0.1-SNAPSHOT-jar-with-dependencies.jar target/batmobile.jar
main_jar=$(find ./target -name "spark*jar")
mv $main_jar target/batmobile.jar
