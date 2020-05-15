# Assumes no processing needed for SQL file
image=postgresql:12.2

exec docker run -p 5432:5432 -e TZ=Etc/UTC $image

case $# in
0) exec docker run -p 5432:5432 -e TZ=Etc/UTC -v "$(git rev-parse --show-toplevel)/kotlin-micronaut/src/main/resources/db/migration":/docker-entrypoint-initdb.d $image ;;
*) exec docker run -p 5432:5432 -e TZ=Etc/UTC $image ;;
esac
