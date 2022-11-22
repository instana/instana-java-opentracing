#!/usr/bin/env bash
set -euf -o pipefail

# Import gpg signing key and password
echo "${TEAM_JAVA_SIGN_PASSWORD}" > pass.txt
echo "${TEAM_JAVA_SIGN_KEY}" > sign.key
gpg --batch --passphrase-file pass.txt --import sign.key

# Release
mvn -P release clean verify deploy

