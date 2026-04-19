#!/bin/sh

/tmp/bootstrap/init.sh &

consul agent -dev -client=0.0.0.0