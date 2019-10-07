CREATE TABLE parent
(
    id         SERIAL PRIMARY KEY,
    natural_id VARCHAR NOT NULL UNIQUE,
    value      VARCHAR,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

INSERT INTO parent
    (natural_id)
VALUES
    ('clarinet');

ALTER TABLE parent
    ADD COLUMN version INT;

UPDATE parent
   SET version = 1;

CREATE TABLE child
(
    id          SERIAL PRIMARY KEY,
    natural_id  VARCHAR NOT NULL UNIQUE,
    parent_id   INT REFERENCES parent (id), -- Nullable
    value       VARCHAR,
    subchildren JSON    NOT NULL,
    version     INT,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

CREATE OR REPLACE FUNCTION upsert_parent(_natural_id parent.natural_id%TYPE,
                                         _value parent.value%TYPE,
                                         _version parent.version%TYPE)
    RETURNS SETOF PARENT
    ROWS 1
    LANGUAGE plpgsql
AS
$$
BEGIN
    RAISE NOTICE 'UPSERT PARENT -- natural_id: %; value: %; version: %',
        _natural_id, _value, _version;

    RETURN QUERY INSERT INTO parent
        (natural_id, value, version)
        VALUES
            (_natural_id, _value, _version)
        ON CONFLICT (natural_id) DO UPDATE
            SET (value, version)
                = (excluded.value, excluded.version)
        RETURNING *;
END;
$$;

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

CREATE OR REPLACE FUNCTION insert_parent_audit_f()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
DECLARE
    now TIMESTAMP DEFAULT now();
BEGIN
    RAISE NOTICE 'INSERT PARENT AUDIT -- NEW: %', new;

    -- While running upsert do on conflict, both insert and update triggers are fired
    -- Check for this case, and do not overwrite the existing audit columns
    -- When an existing row, the update trigger will handle everything
    PERFORM * FROM parent WHERE natural_id = new.natural_id;
    IF FOUND THEN
        RETURN new;
    END IF;

    new.version := 1;
    new.created_at := now;
    new.updated_at := now;
    RETURN new;
END;
$$;

CREATE OR REPLACE FUNCTION insert_child_audit_f()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
DECLARE
    now TIMESTAMP DEFAULT now();
BEGIN
    RAISE NOTICE 'INSERT CHILD AUDIT -- NEW: %', new;

    -- While running upsert do on conflict, both insert and update triggers are fired
    -- Check for this case, and do not overwrite the existing audit columns
    -- When an existing row, the update trigger will handle everything
    PERFORM * FROM child WHERE natural_id = new.natural_id;
    IF FOUND THEN
        RETURN new;
    END IF;

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
    RAISE NOTICE 'UPDATE AUDIT -- NEW: %; OLD: %', new, old;

    old_hash := md5(CAST((old.*) AS TEXT));
    new_hash := md5(CAST((new.*) AS TEXT));

    IF (old_hash = new_hash) THEN
        RETURN NULL; -- Ignore the update, stop processing triggers
    END IF;


    IF (new.version <> old.version) THEN
        RAISE 'Outdated: NEW: %, OLD: %', new, old;
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
    UPDATE parent
       SET updated_at = now() -- Fire the UPDATE trigger of parent, to update audit/version
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
    UPDATE parent
       SET updated_at = now() -- Fire the UPDATE trigger of parent, to update audit/version
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
    UPDATE parent
       SET updated_at = now() -- Fire the UPDATE trigger of parent, to update audit/version
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
EXECUTE PROCEDURE insert_parent_audit_f();

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
EXECUTE PROCEDURE insert_child_audit_f();

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
