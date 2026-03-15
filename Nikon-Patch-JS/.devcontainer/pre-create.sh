#!/bin/sh
# if the .env file does not exist, copy the template and then raise an error so that the user knows to update it
if [ ! -f .devcontainer/.env ]; then
    export USER_UPPER=`echo $USER | tr '[:lower:]' '[:upper:]'`
    # replace all instances of "<username>" with the above username
    sed "s/<username>/${USER_UPPER}/" .devcontainer/.env.example > .devcontainer/.env
    echo "The .env file has been created under the .devcontainer directory. Please update it with the required values."
    exit 1
fi
