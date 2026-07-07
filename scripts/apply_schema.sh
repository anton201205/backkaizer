#!/usr/bin/env bash
set -euo pipefail

# Apply all SQL files in ./sql to a Postgres database.
# Usage:
#   PG_CONN="postgres://user:pass@host:port/dbname" ./scripts/apply_schema.sh
# Or set environment variables: PG_HOST, PG_PORT, PG_USER, PG_DB, PGPASSWORD

if [ -z "${PG_CONN:-}" ]; then
  if [ -n "${PG_HOST:-}" ] && [ -n "${PG_USER:-}" ] && [ -n "${PG_DB:-}" ]; then
    PG_PORT="${PG_PORT:-5432}"
    export PGPASSWORD="${PGPASSWORD:-$PG_PASS}"
    psql "postgresql://${PG_USER}:${PGPASSWORD}@${PG_HOST}:${PG_PORT}/${PG_DB}" -v ON_ERROR_STOP=1 -f /dev/null >/dev/null 2>&1 || true
    CONN="postgresql://${PG_USER}:${PGPASSWORD}@${PG_HOST}:${PG_PORT}/${PG_DB}"
  else
    echo "Error: set PG_CONN or PG_HOST+PG_USER+PG_DB and PGPASSWORD." >&2
    echo "Example: PG_CONN='postgres://user:pass@host:5432/db' $0" >&2
    exit 2
  fi
else
  CONN="$PG_CONN"
fi

echo "Applying SQL files from ./sql to: $CONN"

for f in sql/*.sql; do
  echo "-- Applying $f"
  psql "$CONN" -v ON_ERROR_STOP=1 -f "$f"
done

echo "Schema applied successfully."
