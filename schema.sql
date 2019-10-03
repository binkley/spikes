CREATE TABLE parent
(
    id         SERIAL PRIMARY KEY,
    natural_id VARCHAR NOT NULL UNIQUE,
    value      VARCHAR,
    version    INT
);

CREATE TABLE child
(
    id         SERIAL PRIMARY KEY,
    natural_id VARCHAR NOT NULL UNIQUE,
    parent_id  INT REFERENCES parent (id),
    value      VARCHAR,
    version    INT
);

CREATE OR REPLACE FUNCTION insert_version_f()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$BODY$
BEGIN
    new.version := 1;
    RETURN new;
END;
$BODY$;

CREATE OR REPLACE FUNCTION update_version_f()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$BODY$
BEGIN
    new.version := new.version + 1;
    RETURN new;
END;
$BODY$;

CREATE OR REPLACE FUNCTION insert_child_parent_f()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$BODY$
BEGIN
    UPDATE parent
       SET version = version
     WHERE id = new.parent_id;
    RETURN new;
END;
$BODY$;

CREATE OR REPLACE FUNCTION update_child_parent_f()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$BODY$
BEGIN
    UPDATE parent
       SET version = version
     WHERE id IN (old.parent_id, new.parent_id);
    RETURN new;
END;
$BODY$;

CREATE OR REPLACE FUNCTION delete_child_parent_f()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$BODY$
BEGIN
    UPDATE parent
       SET version = version
     WHERE id = old.parent_id;
    RETURN old;
END;
$BODY$;

CREATE TRIGGER insert_parent_version_t
    BEFORE INSERT
    ON parent
    FOR EACH ROW
EXECUTE PROCEDURE insert_version_f();

CREATE TRIGGER update_parent_version_t
    BEFORE UPDATE
    ON parent
    FOR EACH ROW
EXECUTE PROCEDURE update_version_f();

CREATE TRIGGER insert_child_version_t
    BEFORE INSERT
    ON child
    FOR EACH ROW
EXECUTE PROCEDURE insert_version_f();

CREATE TRIGGER update_child_version_t
    BEFORE UPDATE
    ON child
    FOR EACH ROW
EXECUTE PROCEDURE update_version_f();

CREATE TRIGGER insert_child_parent_t
    AFTER INSERT
    ON child
    FOR EACH ROW
EXECUTE PROCEDURE insert_child_parent_f();

CREATE TRIGGER update_child_parent_t
    AFTER UPDATE
    ON child
    FOR EACH ROW
EXECUTE PROCEDURE update_child_parent_f();

CREATE TRIGGER delete_child_parent_t
    AFTER DELETE
    ON child
    FOR EACH ROW
EXECUTE PROCEDURE delete_child_parent_f();
