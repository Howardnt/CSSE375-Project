#!/bin/bash
# Launches the linter GUI on Mac/Linux using the host machine's Java (17+).
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
java -jar "$DIR/linter.jar"
