#!/bin/sh

if [ $# -ne 4 ]
then
    echo "Error in $0 - Wrong Argument Count"
    echo "Syntax: $0 <groupId> <artefactId> <version> <propertiesFile>"
	echo "Example: $0 com.agifac.maf maf-ldap 1.0.0-SNAPSHOT localvm.properties"
    exit
fi

export GROUPID=$1
export ARTEFACTID=$2
export VERSION=$3
export PROPS=$4

mvn -e clean package -Dsource.groupid=${GROUPID} -Dsource.artefactid=${ARTEFACTID} -Dsource.version=${VERSION} -Dproperty.file=${PROPS}




