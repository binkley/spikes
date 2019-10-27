#!/usr/bin/env bash

set -e

cd ~/tmp

rm -rf layers.git layers
git init --bare layers.git
git clone layers.git

cd layers

git commit --allow-empty -m Init
git push -u origin master
