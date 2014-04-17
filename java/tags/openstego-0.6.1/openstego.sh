#!/bin/sh
DIR=$( (cd -P $(dirname $0) && pwd) )
java -Xmx512m -jar ${DIR}/lib/openstego.jar $*
