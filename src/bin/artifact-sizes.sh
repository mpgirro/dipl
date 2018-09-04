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
    "echo-core/build/libs/echo-core-1.0-SNAPSHOT.jar"
    "echo-actors/build/libs/echo-actors-1.0-SNAPSHOT.jar"
	"echo-actors/build/libs/echo-actors-1.0-SNAPSHOT-all.jar"
	"echo-microservices/app-catalog/build/libs/app-catalog-1.0-SNAPSHOT.jar.original"
	"echo-microservices/app-catalog/build/libs/app-catalog-1.0-SNAPSHOT-all.jar"
	"echo-microservices/app-index/build/libs/app-index-1.0-SNAPSHOT.jar.original"
	"echo-microservices/app-index/build/libs/app-index-1.0-SNAPSHOT-all.jar"
	"echo-microservices/app-searcher/build/libs/app-searcher-1.0-SNAPSHOT.jar.original"
	"echo-microservices/app-searcher/build/libs/app-searcher-1.0-SNAPSHOT-all.jar"
	"echo-microservices/app-crawler/build/libs/app-crawler-1.0-SNAPSHOT.jar.original"
	"echo-microservices/app-crawler/build/libs/app-crawler-1.0-SNAPSHOT-all.jar"
	"echo-microservices/app-parser/build/libs/app-parser-1.0-SNAPSHOT.jar.original"
	"echo-microservices/app-parser/build/libs/app-parser-1.0-SNAPSHOT-all.jar"
	"echo-microservices/app-updater/build/libs/app-updater-1.0-SNAPSHOT.jar.original"
	"echo-microservices/app-updater/build/libs/app-updater-1.0-SNAPSHOT-all.jar"
	"echo-microservices/app-registry/build/libs/app-registry-1.0-SNAPSHOT.jar.original"
	"echo-microservices/app-registry/build/libs/app-registry-1.0-SNAPSHOT-all.jar"
	"echo-microservices/app-gateway/build/libs/app-gateway-1.0-SNAPSHOT.jar.original"
	"echo-microservices/app-gateway/build/libs/app-gateway-1.0-SNAPSHOT-all.jar"
)


for SRC in "${arr[@]}"
do
	BYTES=`stat -f "%z" ~/repos/tu/dipl/src/$SRC`
	#echo -e "$BYTES $SRC"
	echo $(to_kb $BYTES)" KB $SRC"
done
