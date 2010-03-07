#!/bin/bash
export CLASSPATH=./nano­xml­lite.jar:.
# en el directorio /usr/bin esta instalado el OpenJDK
export PATH=$PATH:/usr/bin
java nodo $*
