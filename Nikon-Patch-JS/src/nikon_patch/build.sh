#!/bin/sh

# find location of script and change into that directory, so this script can be run from the porject root.
SCRIPT_DIR="$(dirname "$0")"
#echo '$SCRIPT_DIR'
cd "$SCRIPT_DIR"
#echo 'now in ' $(pwd)

../../emsdk/upstream/emscripten/emcc nikon_patch.c patches.c md5.c md5driver.c xor.c -o nikon_patch.js -s WASM=1 -Oz -s EXPORTED_RUNTIME_METHODS=HEAPU8 -s TOTAL_MEMORY=115343360 -s EXPORTED_RUNTIME_METHODS="['getValue']" -s FILESYSTEM=0 -flto -s ENVIRONMENT=web

cp nikon_patch.js ../../public/
cp nikon_patch.wasm ../../public/