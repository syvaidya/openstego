#!/bin/bash

# @app.name@ - Steganography utility to hide messages into cover files
# Copyright 2007-@time.year@ (c) @author.name@ (mailto:@author.mail@)

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"

java -Xmx1024m -jar ${DIR}/lib/openstego.jar $*
