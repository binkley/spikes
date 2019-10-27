#!/usr/bin/env bash

cd ~/tmp

rm -rf layers.git layers
git init --bare layers.git
git clone layers.git
