#!/bin/bash

export POSTGRES_HOST=127.0.0.1
export POSTGRES_PORT=15432

docker-compose down && docker-compose build && docker-compose run --rm --service-ports setup
