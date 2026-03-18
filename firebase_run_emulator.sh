#!/bin/bash

if test -f .env; then
  source .env
else
  echo "Configure your local '.env' file first. See '.env_example'"
  exit
fi

cd firebase/functions
firebase emulators:start --only auth,functions
