#!/bin/sh

if [ $# -ne 2 ]
then
    echo "Error in $0 - Wrong Argument Count"
    echo "Syntax: $0 <file1> <file2>"
	echo "Example: $0 local.properties empty.properties"
    exit
fi

echo "Executing $0"

export FILE1=$1
export FILE2=$2

cat ${FILE1} | sed '/^\s*$/d' | grep -v "#" | cut -d \= -f 1 | sort > /tmp/compare-props-1.keys
cat ${FILE2} | sed '/^\s*$/d' | grep -v "#" | cut -d \= -f 1 | sort > /tmp/compare-props-2.keys

echo "Comparing file..."

diff /tmp/compare-props-1.keys /tmp/compare-props-2.keys >/dev/null 2>&1
if [ $? -eq 0 ]
then
    echo "Properties files have the same keys. OK."
    exit 0
else
    echo "Properties files have different keys. KO."
    diff /tmp/compare-props-1.keys /tmp/compare-props-2.keys
    exit -1
fi
