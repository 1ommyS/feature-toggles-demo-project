#!/usr/bin/env sh
set -eu

GRADLE_VERSION="${GRADLE_VERSION:-8.14.3}"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
BOOTSTRAP_DIR="$GRADLE_USER_HOME/demo-bootstrap"
DIST_DIR="$BOOTSTRAP_DIR/gradle-$GRADLE_VERSION"
ZIP="$BOOTSTRAP_DIR/gradle-$GRADLE_VERSION-bin.zip"
URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"

if [ ! -x "$DIST_DIR/bin/gradle" ]; then
  mkdir -p "$BOOTSTRAP_DIR"
  echo "Downloading Gradle $GRADLE_VERSION..."
  curl -fsSL "$URL" -o "$ZIP"
  EXPECTED="$(curl -fsSL "$URL.sha256" | tr -d '[:space:]')"
  ACTUAL="$(sha256sum "$ZIP" | awk '{print $1}')"
  if [ "$EXPECTED" != "$ACTUAL" ]; then
    echo "Gradle distribution checksum mismatch" >&2
    exit 1
  fi
  rm -rf "$DIST_DIR"
  unzip -q "$ZIP" -d "$BOOTSTRAP_DIR"
fi

exec "$DIST_DIR/bin/gradle" "$@"
