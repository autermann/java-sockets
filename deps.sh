#!/bin/bash -e

tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT

function install_github {
	local slug="$1"
	local branch="${2:-master}"
	git clone --recursive -q -b "$branch" https://github.com/$slug.git "$tmp/$slug"
	mvn -q -f "$tmp/$slug/pom.xml" -D skipTests clean source:jar javadoc:jar install
}

install_github "autermann/java-utils"
