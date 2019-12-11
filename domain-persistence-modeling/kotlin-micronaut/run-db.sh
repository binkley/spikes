# Assumes no processing needed for SQL file
exec docker run -p 5432:5432 -e TZ=Etc/UTC -v "$(git rev-parse --show-toplevel)/domain-persistence-modeling/kotlin-micronaut/src/main/resources/db/migration/V1__init.sql":/docker-entrypoint-initdb.d/V1__init.sql postgres:11
