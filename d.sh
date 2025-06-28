#!/bin/bash
mvn clean package -DskipTests
docker build -t bot:latest .
docker run bot
