#!/bin/sh

. $(dirname $0)/env.sh

SCALA_VERSION=2.10.0-SNAPSHOT
SCALA_LIBRARY_VERSION=2.10.0-SNAPSHOT
PROFILE="-P local-scala-trunk,!scala-trunk"

build $*
