#!/bin/bash

set -eo pipefail

echo "$0: How to inject the queue_name so it matches application.yml?" >&2


function create-queue {
    local queue_name="$1"

    awslocal sqs create-queue --queue-name "$queue_name"
}

function list-queue {
    local queue_name="$1"

    awslocal sqs list-queues | grep "http://localhost:4576/queue/$queue_name"
}

function loop-or-fail {
    local action="$1"
    local queue_name="$2"

    case "$action" in
    create | list ) ;;
    * ) echo "$0: BUG: Queue action must be create or list: $@" ; exit 2 ;;
    esac

    # Turns out LocalStack is not always Ready
    sleep 7  # LocalStack uses 7 in its own script

    let n=0 || true
    until $action-queue "$queue_name" >/dev/null
    do
        let n+=1 || true
        if (( n > 3 ))
        then
            printf "$0: FAILED to $action SQS queue after %d attempts: $queue_name" $n
            exit 1
        fi

        printf "$0: Waiting for SQS to $action queue, attempt %d: $queue_name" $n

        sleep 7  # LocalStack uses 7 in its own script
    done
}


# TODO: Why the extracted variable?  It's only used in one place
# TODO: This script duplicates aircraft/docker/create-queues.sh, varying only in queue names
QUEUES=("foo")

for queue_name in "${QUEUES[@]}"
do
    loop-or-fail create "$queue_name"
    loop-or-fail list "$queue_name"

    echo "$0: SQS queue ready to use: $queue_name"
done
