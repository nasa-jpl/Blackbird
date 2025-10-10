#!/bin/sh
javadoc \
    -d docs \
    -link https://docs.oracle.com/en/java/javase/13/docs/api/ \
    -linkoffline https://nasa-jpl.github.io/jplTime ../jplTime/docs \
    -cp $HOME/.m2/repository/gov/nasa/jpl/jpl_time/2025-10a/jpl_time-2025-10a.jar:$HOME/.m2/repository/gov/nasa/jpl/Blackbird/2025-10a/blackbird-2025-10a.jar:$HOME/.m2/repository/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar:$HOME/.m2/repository/com/google/code/gson/gson/2.9.0/gson-2.9.0.jar:$HOME/.m2/repository/org/objenesis/objenesis/3.2/objenesis-3.2.jar:$HOME/.m2/repository/com/google/guava/guava/32.1.3-jre/guava-32.1.3-jre.jar \
    -docletpath $HOME/.m2/repository/nl/talsmasoftware/umldoclet/2.0.8/umldoclet-2.0.8.jar \
    -doclet nl.talsmasoftware.umldoclet.UMLDoclet \
    -sourcepath src/main/java \
    -subpackages gov.nasa.jpl \
    -exclude gov.nasa.jpl.activity.annotations:gov.nasa.jpl.output.csv:gov.nasa.jpl.exampleAdaptation
