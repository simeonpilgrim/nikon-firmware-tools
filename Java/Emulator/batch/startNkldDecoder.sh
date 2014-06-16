#!/bin/bash 
echo "Starting FirmwareDecoder"
java -cp "./NikonEmulator.jar":"./lib/commons-io-2.1.jar":"./lib/commons-lang3-3.1.jar":"./lib/xstream-1.4.2.jar":"./lib/jacksum.jar" com.nikonhacker.encoding.NkldDecoder "$@"

