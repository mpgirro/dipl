#!/usr/bin/env bash

# 1500 -> 1.500
function to_kb {
  	echo $(bc <<< "scale=3; $1/1000")
}

# 10000 -> 10,000
function format_number {
  	printf "%'.3f\n" $1
}

declare -a arr=(
    "echo-actors/build/libs/echo-actors-1.0-SNAPSHOT.jar" 
	"echo-actors/build/libs/echo-actors-1.0-SNAPSHOT-all.jar" 
	"echo-microservices/app-catalog/build/libs/catalog-1.0-SNAPSHOT.jar.original"
	"echo-microservices/app-catalog/build/libs/catalog-1.0-SNAPSHOT-all.jar"
	"echo-microservices/app-index/build/libs/index-1.0-SNAPSHOT.jar.original"
	"echo-microservices/app-index/build/libs/index-1.0-SNAPSHOT-all.jar"
	"echo-microservices/app-searcher/build/libs/searcher-1.0-SNAPSHOT.jar.original"
	"echo-microservices/app-searcher/build/libs/searcher-1.0-SNAPSHOT-all.jar"
	"echo-microservices/app-crawler/build/libs/crawler-1.0-SNAPSHOT.jar.original"
	"echo-microservices/app-crawler/build/libs/crawler-1.0-SNAPSHOT-all.jar"
	"echo-microservices/app-parser/build/libs/parser-1.0-SNAPSHOT.jar.original"
	"echo-microservices/app-parser/build/libs/parser-1.0-SNAPSHOT-all.jar"
)


for SRC in "${arr[@]}"
do
	BYTES=`stat -f "%z" ~/repos/tu/dipl/src/$SRC`
	#echo -e "$BYTES $SRC"
	echo $(to_kb $BYTES)" KB $SRC"
done