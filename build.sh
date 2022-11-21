#!/bin/sh

(
  cd frontend
  apk add npm &&
  npm install &&
  npm run release
) || { echo fail; exit 1; }
