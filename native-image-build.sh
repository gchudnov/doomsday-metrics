#!/usr/bin/env bash

# 20.2.0.r11-grl

# cat ./res/graalvm/init-run-time.txt | tr '\n' ','

#expost BUILD_INIT_LIST=ch.qos.logback.classic.Logger
#  --initialize-at-build-time="${BUILD_INIT_LIST}" \

#sbt "test; doom/assembly"

export RUNTIME_INIT_LIST='io.netty.util.internal.logging.Log4JLogger'

native-image \
  --verbose \
  --initialize-at-build-time \
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
  -jar ./target/doom.jar doom
