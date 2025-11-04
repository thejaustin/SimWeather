#!/usr/bin/env bash

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Determine the Java command to use to start the JVM.
if [ -n ""JAVA_HOME"" ] ; then
    if [ -x ""$JAVA_HOME/jre/sh/java"" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD=""$JAVA_HOME/jre/sh/java""
    else
        JAVACMD=""$JAVA_HOME/bin/java""
    fi
    if [ ! -x ""$JAVACMD"" ] ; then
        die ""ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

In particular, no executable found at ""$JAVA_HOME/bin/java""""
    fi
else
    JAVACMD=""java""
    which java >/dev/null || die ""ERROR: JAVA_HOME is not set and no 'java' command can be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation.""
fi

# Determine the script directory.
SCRIPT_DIR=""$(dirname ""$0"")""

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS=(-Xmx64m -Xms64m)

# Set Gradle properties
GRADLE_OPTS+=(-Dorg.gradle.appname=gradle)

# Combine all JVM arguments
JVM_ARGS=("${DEFAULT_JVM_OPTS[@]}" "${JAVA_OPTS[@]}" "${GRADLE_OPTS[@]}")

# Execute Gradle
exec "$JAVACMD" "${JVM_ARGS[@]}" -classpath "$SCRIPT_DIR/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
