#!/bin/sh
# Installs a modified version of tmc-checkstyle-runner

mvn clean install -U -f tmc-langs-abstraction/pom.xml
git clone https://github.com/rage/tmc-checkstyle-runner.git
mvn clean install -U -f tmc-checkstyle-runner/pom.xml
