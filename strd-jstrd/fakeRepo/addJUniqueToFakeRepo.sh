#!/bin/bash

if [[ $# -ne 1 ]]; then
  echo supply file to add into fake repo;
  exit 1
fi

mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=$1 -DgroupId=it.sauronsoftware -DartifactId=junique -Dversion=1.0.4 -Dpackaging=jar -DlocalRepositoryPath=$(dirname $0)


