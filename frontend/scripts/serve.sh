#!/bin/sh
cd "$(dirname "$0")/.."
export PATH="$PWD/node:$PATH"
exec npm start
