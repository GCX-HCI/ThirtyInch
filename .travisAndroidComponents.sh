#!/bin/bash

# Read the BUILD_TOOL_VERSION and COMPILE_SDK_VERSION
BUILD_TOOLS_VERSION=$(cat build.gradle | grep "BUILD_TOOLS_VERSION*" | sed -e "s/^.*'\(.*\)'/\1/")
COMPILE_SDK_VERSION=$(cat build.gradle | grep "COMPILE_SDK_VERSION*" | sed -e "s/^.*= \(.*\)$/\1/")

# Echo every componnent we need to install and replace the BUILD_TOOLS_VERSION and COMPILE_SDK_VERSION
echo "tools,platform-tools,tools,build-tools-$BUILD_TOOLS_VERSION,android-22,android-$COMPILE_SDK_VERSION,extra-google-google_play_services,extra-google-m2repository,extra-android-m2repository,addon-google_apis-google-24,sys-img-armeabi-v7a-android-22"
