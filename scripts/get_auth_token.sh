#!/bin/sh
# Copyright 2010 Google Inc.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


# Note: on a Mac, you may need to call:
#
#    sudo port install curl +ssl
#
# before running this script.

if [[ "$1" == "-dev" ]]; then
  DEV=1
  DOMAIN=$2
else
  DOMAIN=$1
fi

if [ -z $DOMAIN ]; then
  echo "Usage: $0 [-dev] <server-domain>" >&2
  exit
fi

read -p "Email: " EMAIL

if [ -n "$DEV" ]; then
  # Development server, don't use real auth
  
  # Get a user auth token
  # Get cookies to use in later requests to the server
  echo Getting token from http://$DOMAIN/_ah/login ... >&2
  curl \
      -o /dev/null \
      -d "email=$EMAIL&action=Log+In&isAdmin=on" \
      -c - \
      "http://$DOMAIN/_ah/login" \
      2>/dev/null
else
  # Real server, hit real auth path
  
  # Get user password
  stty -echo
  read -p "Password: " PASSWORD; echo >&2
  stty echo

  # Get a user auth token
  echo Getting token from https://www.google.com/accounts/ClientLogin ... >&2
  AUTH=$(curl \
      https://www.google.com/accounts/ClientLogin \
      -d Email=$EMAIL \
      -d Passwd=$PASSWORD \
      -d accountType=HOSTED_OR_GOOGLE \
      -d source=foobar \
      -d service=ah \
      2>/dev/null \
      | awk -F= '$1 ~ /Auth/ {print $2}')

  if [ -z "${AUTH}" ]; then
    echo "Auth error" >&2
    exit
  fi

  # Get cookies to use in later requests to the server
  echo Getting token from https://$DOMAIN/_ah/login ... >&2
  curl -o /dev/null -c - https://$DOMAIN/_ah/login?auth=$AUTH 2>/dev/null

fi
