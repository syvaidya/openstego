#!/bin/bash

# @app.name@ - Steganography utility to hide messages into cover files
# Copyright 2007-@time.year@ (c) @author.name@ (mailto:@author.mail@)

JAVA_OPTS=-Xmx1024m

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"

java ${JAVA_OPTS} -jar ${DIR}/lib/openstego.jar $*
