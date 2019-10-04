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
    parent_id  INT REFERENCES parent (id), -- Nullable
    value      VARCHAR,
    version    INT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE OR REPLACE FUNCTION update_immutable_natural_key_f()
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

    IF (new.version <> old.version) THEN
        RAISE 'Outdated: %, %', old.version, new.version;
    END IF;

    new.version := old.version + 1;
    new.updated_at = now();
    RETURN new;
END;
$$;

CREATE OR REPLACE FUNCTION insert_child_update_parent_f()
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

CREATE OR REPLACE FUNCTION update_child_update_parent_f()
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

CREATE OR REPLACE FUNCTION delete_child_update_parent_f()
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

CREATE TRIGGER a_update_parent_immutable_natural_key_t
    BEFORE UPDATE
    ON parent
    FOR EACH ROW
EXECUTE PROCEDURE update_immutable_natural_key_f();

CREATE TRIGGER b_insert_parent_audit_t
    BEFORE INSERT
    ON parent
    FOR EACH ROW
EXECUTE PROCEDURE insert_audit_f();

CREATE TRIGGER b_update_parent_audit_t
    BEFORE UPDATE
    ON parent
    FOR EACH ROW
EXECUTE PROCEDURE update_audit_f();

CREATE TRIGGER a_update_child_immutable_natural_key_t
    BEFORE UPDATE
    ON child
    FOR EACH ROW
EXECUTE PROCEDURE update_immutable_natural_key_f();

CREATE TRIGGER b_insert_child_audit_t
    BEFORE INSERT
    ON child
    FOR EACH ROW
EXECUTE PROCEDURE insert_audit_f();

CREATE TRIGGER b_update_child_audit_t
    BEFORE UPDATE
    ON child
    FOR EACH ROW
EXECUTE PROCEDURE update_audit_f();

CREATE TRIGGER c_insert_child_update_parent_t
    AFTER INSERT
    ON child
    FOR EACH ROW
EXECUTE PROCEDURE insert_child_update_parent_f();

CREATE TRIGGER c_update_child_update_parent_t
    AFTER UPDATE
    ON child
    FOR EACH ROW
EXECUTE PROCEDURE update_child_update_parent_f();

CREATE TRIGGER c_delete_child_update_parent_t
    AFTER DELETE
    ON child
    FOR EACH ROW
EXECUTE PROCEDURE delete_child_update_parent_f();
