#!/usr/bin/env bash

set -e

# import setup functions
. /app/build/setup_db_scripts.sh

time setup_postgres $POSTGRES_SCRIPT postgres


echo "Databases are ready!"
