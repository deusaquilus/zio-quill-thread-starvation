#!/usr/bin/env bash

set -e

echo "### Bringing Down Any Docker Containers that May Be Running ###"
time docker-compose down --rmi all

echo "### Bringing Up sqlserver, oracle, postgres, mysql Images ###"
time docker-compose up -d postgres
echo "### DONE Bringing Up sqlserver and oracle Images ###"

echo "### Checking Docker Images"
docker ps

# import setup functions
echo "### Sourcing DB Scripts ###"
. build/setup_db_scripts.sh

echo "### Running Setup for postgres ###"
time setup_postgres $POSTGRES_SCRIPT 127.0.0.1 15432


echo "Oracle Setup Complete"

echo "Databases are ready!"