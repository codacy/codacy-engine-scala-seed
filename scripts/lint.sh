#!/usr/bin/env bash

set -e

sbt scalafmtCheck
sbt scapegoat
