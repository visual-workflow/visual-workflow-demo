#!/usr/bin/env bash

cd ../visual-workflow-visualizer
mvn package exec:java -DskipTests -Dexec.mainClass=com.kgignatyev.temporal.visualwf.renderer.VisualizeWF
cd -
