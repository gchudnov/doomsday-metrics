#!/usr/bin/env bash

# 20.2.0.r11-grl

#sbt "test; doom/assembly"

BUILD_INIT_LIST="$(cat ./res/graalvm/init-build-time.txt | tr '\n' ',')"
RUNTIME_INIT_LIST="$(cat ./res/graalvm/init-run-time.txt | tr '\n' ',')"

native-image \
  --verbose \
  --initialize-at-build-time="${BUILD_INIT_LIST}" \
  --initialize-at-run-time="${RUNTIME_INIT_LIST}" \
  --no-fallback \
  --allow-incomplete-classpath \
  --enable-http \
  --enable-https \
  -H:+ReportUnsupportedElementsAtRuntime \
  -H:+ReportExceptionStackTraces \
  -H:ResourceConfigurationFiles=./res/graalvm/resources.json \
  -H:ReflectionConfigurationFiles=./res/graalvm/reflection.json \
  -H:+TraceClassInitialization \
  -H:+StackTrace \
  -jar ./target/doom.jar doom-cli
