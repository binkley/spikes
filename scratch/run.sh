#!/usr/bin/env bash
# shellcheck disable=SC2214,SC2215

export PS4='+${BASH_SOURCE}:${LINENO}:${FUNCNAME[0]:+${FUNCNAME[0]}():} '

set -e
set -u
set -o pipefail

package=x.scratch
jar=target/scratch-0-SNAPSHOT-jar-with-dependencies.jar

function mangle-classname() {
    local IFS=.

    local -a parts
    read -r -a parts <<<"$1"
    local last="${parts[-1]}"

    case "$last" in
    *-* | *Kt) ;;
    *) last="${last}Kt" ;;
    esac
    last="${last//-/_}"
    last=""${last^}

    parts[-1]="$last"

    echo "${parts[*]}"
}

function rebuild-if-needed() {
    [[ ! -e "$jar" || -n "$(find src -type f -newer "$jar")" ]]
}

debug=false
while getopts :d-: opt; do
    [[ $opt == - ]] && opt=${OPTARG%%=*} OPTARG=${OPTARG#*=}
    case $opt in
    d | debug) debug=true ;;
    *) exit 2 ;;
    esac
done
shift $((OPTIND - 1))

$debug && set -x

case $# in
0) set - -jar "$jar" ;;
*)
    class="$1"
    shift
    set - -cp "$jar" "$(mangle-classname "$package.$class")" "$@"
    ;;
esac
$debug && set -x # "set - ..." clears the -x flag

rebuild-if-needed && ./mvnw -C package

exec java "$@"
