#!/usr/bin/env bash
#
# Starts a JVM and runs HeadlessModel, which supports JVM integration via py4j.

set -eu

# source: https://stackoverflow.com/questions/59895/get-the-source-directory-of-a-bash-script-from-within-the-script-itself#246128
MY_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

java -cp "${MY_DIR}"/target/housing-model-1.0-SNAPSHOT-jar-with-dependencies10006.jar housing.HeadlessModel