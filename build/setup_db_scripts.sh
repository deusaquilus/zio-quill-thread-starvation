export POSTGRES_SCRIPT=src/test/sql/create.sql

function setup_postgres() {
    port=$3
    if [ -z "$port" ]; then
        echo "Postgres Port not defined. Setting to default: 5432"
        port="5432"
    else
        echo "Postgres Port specified as $port"
    fi
    echo "Waiting for Postgres"
    until psql --host $2 --port $port --username postgres -c "select 1" &> /dev/null; do
        echo "## Tapping Postgres Connection> psql --host $2 --port $port --username postgres -c 'select 1'"
        psql --host $2 --port $port --username postgres -c "select 1" || true
        sleep 5;
    done
    echo "Connected to Postgres"

    echo "Postgres: Create codegen_test"
    psql --host $2 --port $port -U postgres -c "CREATE DATABASE codegen_test"
    echo "Postgres: Create quill_test"
    psql --host $2 --port $port -U postgres -c "CREATE DATABASE quill_test"
    echo "Postgres: Write Schema to quill_test"
    psql --host $2 --port $port -U postgres -d quill_test -a -q -f $1
}

export -f setup_postgres
