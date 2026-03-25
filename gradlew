#!/bin/sh
##############################################################################
#
#   Gradle start up script for POSIX
#
##############################################################################

APP_HOME=$(cd "$(dirname "$0")" ; pwd -P)
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
exec java $JAVA_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
