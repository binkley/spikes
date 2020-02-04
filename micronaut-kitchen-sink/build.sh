#!/bin/sh

# TODO: Get rid of this script when I can pass JVM args to Kotlinc
exec ./gradlew clean build -Dmicronaut.openapi.views.spec=swagger-ui.enabled=true,swagger-ui.theme=flattop
