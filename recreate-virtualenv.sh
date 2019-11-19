#!/usr/bin/env bash

set -eu

# source: https://stackoverflow.com/questions/59895/get-the-source-directory-of-a-bash-script-from-within-the-script-itself#246128
MY_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

PYTHON="python3"
VENV_NAME="venv"

# delete the current virtualenv, if it exists
rm -rf "${MY_DIR}"/"${VENV_NAME}"

# create a pristine virtualenv
"${PYTHON}" -m venv --copies "${MY_DIR}/${VENV_NAME}"
"${MY_DIR}/${VENV_NAME}"/bin/pip install --upgrade pip
"${MY_DIR}/${VENV_NAME}"/bin/pip install -r "${MY_DIR}"/requirements.txt

"${MY_DIR}/install-py4j-in-local-maven.sh"
