#!/bin/bash

set -e

echo "Generating crypto material..."
cryptogen generate --config=./crypto-config.yaml --output=crypto-config

echo "Setting FABRIC_CFG_PATH..."
export FABRIC_CFG_PATH=$PWD

echo "Creating genesis block..."
configtxgen \
-profile Genesis \
-channelID system-channel \
-outputBlock ./genesis.block

echo "Starting Fabric containers..."
docker-compose up -d

echo "Network started successfully!"
docker ps