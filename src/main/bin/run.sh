#!/bin/sh
#  DataRecorder unix startup script
#


# Use these opts for remote debugging
# JAVA_OPTS="-Xms256m -Xmx512m -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# Virtual Machine Arguments
VMARGS=
VMARGS=$VMARGS -Dpublisher.suppress.start.timestamp=false
VMARGS=$VMARGS -Dsubscriber.insert.start.timestamp=false

# Classpath
CLASSPATH=../conf

for i in `find ../lib -name *.jar`; do CLASSPATH=$CLASSPATH:$i;done
for i in `find ../lib/ext -name *.jar`; do CLASSPATH=$CLASSPATH:$i;done
export CLASSPATH

# Class to execute
CLASS='com.boeing.datarecorder.DataRecorder'

java $JAVA_OPTS $VMARGS -cp $CLASSPATH $CLASS

