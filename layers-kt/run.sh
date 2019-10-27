#!/usr/bin/env bash

set -e

cd ~/tmp

rm -rf layers.git layers
git init --bare layers.git
git clone layers.git

cd layers

cat >README.md <<EOM
# Working directory for Layers
EOM
git add .
git commit -m Init
git push -u origin master
