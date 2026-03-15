#!/bin/sh

# From here: https://emscripten.org/docs/getting_started/downloads.html#installation-instructions-using-the-emsdk-recommended

# Get the emsdk repo
REPO_URL="https://github.com/emscripten-core/emsdk.git"
# Extract the repository name from the URL to use as the folder name
REPO_FOLDER=$(basename "$REPO_URL" .git)

if [ ! -d "$REPO_FOLDER" ]; then
    echo "Directory $REPO_FOLDER does not exist. Cloning repository..."
    git clone "$REPO_URL"
    cd "$REPO_FOLDER"
else
    echo "Directory $REPO_FOLDER already exists. Pulling latest changes..."
    cd "$REPO_FOLDER"
    git pull
fi

# Download and install the latest SDK tools.
./emsdk install latest

# Make the "latest" SDK "active" for the current user. (writes .emscripten file)
./emsdk activate latest

# Activate PATH and other environment variables in the current terminal
source ./emsdk_env.sh

# and set for future shells:
LINE_TO_ADD='source "/workspaces/nikon-firmware-tools/Nikon-Patch-JS/emsdk/emsdk_env.sh'
LINE_TO_SEARCH='emsdk/emsdk_env.sh'
PROFILE_FILE=$HOME/.bash_profile

if ! grep -qF "$LINE_TO_SEARCH" "$PROFILE_FILE"; then
  # If not found, append the line to the file
  echo "$LINE_TO_ADD" >> "$PROFILE_FILE"
  echo "Added line to $PROFILE_FILE"
else
  echo "Line already exists in $PROFILE_FILE"
fi