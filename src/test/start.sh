#!/bin/bash
mvn -U clean package -DskipTests -DapiKey=$RAPIDAPI_KEY
docker-compose up --build --detach