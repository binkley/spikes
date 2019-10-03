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
$$
BEGIN
    IF (new.natural_id <> old.natural_id) THEN
        RAISE 'Cannot change the natural key';
    END IF;
    RETURN new;
END;
$$;

CREATE OR REPLACE FUNCTION insert_audit_f()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
DECLARE
    now TIMESTAMP DEFAULT now();
BEGIN
    new.version := 1;
    new.created_at := now;
    new.updated_at := now;
    RETURN new;
END;
$$;

CREATE OR REPLACE FUNCTION update_audit_f()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
DECLARE
    old_hash VARCHAR;
    new_hash VARCHAR;
BEGIN
    old_hash := md5(CAST((old.*) AS TEXT));
    new_hash := md5(CAST((new.*) AS TEXT));

    -- Ignore the update, stop processing triggers
    IF (old_hash = new_hash) THEN
        RETURN NULL;
    END IF;

    -- Ignore if caller tried to replace the version, so "old.version + 1"
    new.version := old.version + 1;
    new.updated_at = now();
    RETURN new;
END;
$$;

CREATE OR REPLACE FUNCTION insert_child_parent_f()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    -- Pick a column that:
    -- 1) Really changes, so "ignore unchanged" does not kick in
    -- 2) Is not user-data visible, so remains truly an "audit" field
    UPDATE parent
    SET updated_at = now()
    WHERE id = new.parent_id;
    RETURN new;
END;
$$;

CREATE OR REPLACE FUNCTION update_child_parent_f()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    -- Pick a column that:
    -- 1) Really changes, so "ignore unchanged" does not kick in
    -- 2) Is not user-data visible, so remains truly an "audit" field
    UPDATE parent
    SET updated_at = now()
    WHERE id IN (old.parent_id, new.parent_id);
    RETURN new;
END;
$$;

CREATE OR REPLACE FUNCTION delete_child_parent_f()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    -- Pick a column that:
    -- 1) Really changes, so "ignore unchanged" does not kick in
    -- 2) Is not user-data visible, so remains truly an "audit" field
    UPDATE parent
    SET updated_at = now()
    WHERE id = old.parent_id;
    RETURN old;
END;
$$;

CREATE TRIGGER immutable_parent_natural_key_t
    BEFORE UPDATE
    ON parent
    FOR EACH ROW
EXECUTE PROCEDURE immutable_natural_key_f();

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

CREATE TRIGGER immutable_child_natural_key_t
    BEFORE UPDATE
    ON child
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
