#!/bin/sh

echo "bootstrap values - wait until port is available"

while ! nc -z localhost 8500; do
  sleep 1
done

echo "executing consul kv command"

consul kv import @/tmp/bootstrap/values.json