FROM mcr.microsoft.com/devcontainers/javascript-node:20
# Point at any base image that you find suitable to extend.
#FROM https://hub.docker.com/r/emscripten/emsdk:1.38.25

# Install required tools that are useful for your project i.e. ninja-build
RUN apt update && apt install -y ninja-build

# Update package lists, install p7zip-full, and clean up apt lists
RUN apt-get update -y && \
    apt-get install -y p7zip-full && \
    rm -rf /var/lib/apt/lists/*