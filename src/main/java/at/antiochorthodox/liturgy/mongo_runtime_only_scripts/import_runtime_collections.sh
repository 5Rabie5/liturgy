#!/usr/bin/env bash
set -euo pipefail

DB_NAME="${1:-liturgydb}"
DATA_DIR="${2:-./data}"

mongoimport --db "$DB_NAME" --collection liturgical_reading_assignments --file "$DATA_DIR/final_liturgical_reading_assignments.json" --jsonArray --drop
mongoimport --db "$DB_NAME" --collection gospel_readings --file "$DATA_DIR/final_gospel_readings.json" --jsonArray --drop
mongoimport --db "$DB_NAME" --collection epistle_readings --file "$DATA_DIR/final_epistle_readings.json" --jsonArray --drop
mongoimport --db "$DB_NAME" --collection liturgical_labels --file "$DATA_DIR/final_liturgical_labels.json" --jsonArray --drop

echo "Imported runtime-only collections into $DB_NAME"
