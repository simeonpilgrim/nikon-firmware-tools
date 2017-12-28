REM emcc nikon_patch.c -o nikon_patch.js -s WASM=1 -s EXPORTED_FUNCTIONS="['_main','_detectFirmware']" -s "BINARYEN_METHOD='native-wasm'" -s EXTRA_EXPORTED_RUNTIME_METHODS="['ccall','cwrap']" -O2 -s NO_EXIT_RUNTIME=1
emcc nikon_patch.c md5.c md5driver.c -o nikon_patch.js -s WASM=1 -O3 -s TOTAL_MEMORY=73400320 -s EXTRA_EXPORTED_RUNTIME_METHODS="['getValue']"
