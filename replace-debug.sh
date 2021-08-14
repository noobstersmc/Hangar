#!/bin/bash
chcp.com 65001
# Build
./gradlew build
# Copy the plugin into folder
cp build/libs/*-all.jar debug/plugins/*-all.jar
