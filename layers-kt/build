#!/bin/bash

unset KOTLIN_HOME

(( 0 == $# )) && set -- clean verify

exec ./mvnw -Dversions-maven-plugin.phase=none "$@"
