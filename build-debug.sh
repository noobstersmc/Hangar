#!/bin/bash
set -e
# Set encoding to UTF-8
chcp.com 65001
# Build the project
./gradlew build
# Setup debug environment
sh setup-debug.sh
# Start debug server
cd debug
sh start-debug.sh
