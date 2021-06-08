#!/bin/bash


cd "$(dirname "$0")"
cd ..

mvn clean compile assembly:single
main_jar=$(find ./target -name "spark*jar")
mv $main_jar target/thanou.jar
