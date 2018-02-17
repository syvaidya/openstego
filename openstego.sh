#!/bin/sh

DIR=$( (cd -P $(dirname $0) && pwd) )
java -Xmx1024m -jar ${DIR}/lib/openstego.jar $*
