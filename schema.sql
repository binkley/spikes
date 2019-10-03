CREATE TABLE parent
(
    id         SERIAL PRIMARY KEY,
    natural_id VARCHAR NOT NULL UNIQUE,
    value      VARCHAR,
    version    INT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE child
(
    id         SERIAL PRIMARY KEY,
    natural_id VARCHAR NOT NULL UNIQUE,
    parent_id  INT REFERENCES parent (id),
    value      VARCHAR,
    version    INT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE OR REPLACE FUNCTION immutable_natural_key_f()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$BODY$
BEGIN
    IF (new.natural_id <> old.natural_id) THEN
        RAISE 'Cannot change the natural key';
    END IF;
    RETURN new;
END;
$BODY$;

CREATE OR REPLACE FUNCTION insert_audit_f()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$BODY$
BEGIN
    new.version := 1;
    new.created_at := now();
    RETURN new;
END;
$BODY$;

CREATE OR REPLACE FUNCTION update_audit_f()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$BODY$
BEGIN
    new.version := new.version + 1;
    new.updated_at = now();
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

CREATE TRIGGER insert_parent_audit_t
    BEFORE INSERT
    ON parent
    FOR EACH ROW
EXECUTE PROCEDURE insert_audit_f();

CREATE TRIGGER update_parent_audit_t
    BEFORE UPDATE
    ON parent
    FOR EACH ROW
EXECUTE PROCEDURE update_audit_f();

CREATE TRIGGER immutable_parent_natural_key_t
    AFTER UPDATE
    ON parent
    FOR EACH ROW
EXECUTE PROCEDURE immutable_natural_key_f();

CREATE TRIGGER insert_child_audit_t
    BEFORE INSERT
    ON child
    FOR EACH ROW
EXECUTE PROCEDURE insert_audit_f();

CREATE TRIGGER update_child_audit_t
    BEFORE UPDATE
    ON child
    FOR EACH ROW
EXECUTE PROCEDURE update_audit_f();

CREATE TRIGGER immutable_child_natural_key_t
    AFTER UPDATE
    ON child
    FOR EACH ROW
EXECUTE PROCEDURE immutable_natural_key_f();

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
