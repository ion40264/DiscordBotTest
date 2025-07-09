#!/bin/bash

npm run build
mvn clean package -DskipTests
docker build -t bot:latest .
docker run bot
