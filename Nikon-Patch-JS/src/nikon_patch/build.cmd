emcc nikon_patch.c md5.c md5driver.c xor.c -o nikon_patch.js -s WASM=1 -O3 -s TOTAL_MEMORY=115343360 -s EXTRA_EXPORTED_RUNTIME_METHODS="['getValue']"
