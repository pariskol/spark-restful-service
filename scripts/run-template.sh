#!/bin/bash

cd "$(dirname "$0")"

/usr/lib/jvm/java-1.8.0-openjdk-amd64/bin/java -Xmx128m -cp <my jar>:lib/* <my main class>
