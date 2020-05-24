This project was bootstrapped with [Create React App](https://github.com/facebookincubator/create-react-app).

To build
========

Emscripten install
------------------

  1. you need working Emscripten install, https://developer.mozilla.org/en-US/docs/WebAssembly/C_to_wasm
  2. which points to https://emscripten.org/docs/getting_started/downloads.html
     
     which say to:
      1. git clone https://github.com/emscripten-core/emsdk.git
      2. cd emsdk
      3. ./emsdk install latest
      4. ./emsdk activate latest
      5. source ./emsdk_env.sh 
	  
    (On Windows, run emsdk instead of ./emsdk, and emsdk_env.bat instead of source ./emsdk_env.sh.)

Build C to WebAssembly
----------------------

  1. cd \Nikon-Patch-JS\src\nikon_patch
  2. build.cmd 
  3. cp.cmd (to put the wasm into the react project)
  
Build React App
---------------

  1. cd \Nikon-Patch-JS\
  2. npm install
  3. npm start 
  4. npm run build  (to build deployment)
  5. rename index.html to nikon-patch.html

Patch Development
=================

Added/Update alter the C# project in \Nikon-Patch\Nikob-Patch.sln (the LocalUI project) and test/dev via that, the export via C# patch code to C patch.c with the C# tool. 