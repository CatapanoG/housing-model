#!/usr/bin/env bash
#
# This is a support file used by recreate-virtualenv.sh
set -eu

# source: https://stackoverflow.com/questions/59895/get-the-source-directory-of-a-bash-script-from-within-the-script-itself#246128
MY_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# Source: https://stackoverflow.com/questions/4955635/how-to-add-local-jar-files-to-a-maven-project#4955695
mvn install:install-file \
   -Dfile="${MY_DIR}"/venv/share/py4j/py4j0.10.8.1.jar \
   -DgroupId=bartdag \
   -DartifactId=py4j \
   -Dversion=0.18.1 \
   -Dpackaging=jar \
   -DgeneratePom=true
