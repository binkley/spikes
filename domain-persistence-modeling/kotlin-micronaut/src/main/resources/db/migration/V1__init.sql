CREATE TABLE other
(
    id         SERIAL PRIMARY KEY,
    natural_id VARCHAR NOT NULL UNIQUE,
    value      VARCHAR,
    -- DB controls Audit columns, not caller
    version    INT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE parent
(
    id               SERIAL PRIMARY KEY,
    natural_id       VARCHAR NOT NULL UNIQUE,
    other_natural_id VARCHAR REFERENCES other (natural_id), -- Nullable
    state            VARCHAR NOT NULL,
    value            VARCHAR,
    -- TODO: side_values      VARCHAR ARRAY NOT NULL,
    side_values      VARCHAR NOT NULL,
    -- DB controls Audit columns, not caller
    version          INT,
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP
);

CREATE TABLE child
(
    id                  SERIAL PRIMARY KEY,
    natural_id          VARCHAR     NOT NULL UNIQUE,
    other_natural_id    VARCHAR REFERENCES other (natural_id),  -- Nullable
    parent_natural_id   VARCHAR REFERENCES parent (natural_id), -- Nullable
    state               VARCHAR     NOT NULL,
    at                  TIMESTAMPTZ NOT NULL,
    value               VARCHAR,
    -- TODO: default_side_values VARCHAR ARRAY NOT NULL,
    default_side_values VARCHAR     NOT NULL,
    -- TODO: side_values         VARCHAR ARRAY NOT NULL,
    side_values         VARCHAR     NOT NULL,
    -- DB controls Audit columns, not caller
    version             INT,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP
);

CREATE OR REPLACE FUNCTION upsert_other(_natural_id other.natural_id%TYPE,
                                        _value other.value%TYPE,
                                        _version other.version%TYPE)
    RETURNS SETOF OTHER
    ROWS 1
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY INSERT INTO other
        (natural_id, value, version)
        VALUES (_natural_id, _value, _version)
        ON CONFLICT (natural_id) DO UPDATE
            SET (value, version)
                = (excluded.value, excluded.version)
        RETURNING *;
END;
$$;

-- Workaround issue in Spring Data with passing sets for ARRAY types in a procedure
CREATE OR REPLACE FUNCTION upsert_parent(_natural_id parent.natural_id%TYPE,
                                         _other_natural_id parent.other_natural_id%TYPE,
                                         _state parent.state%TYPE,
                                         _value parent.value%TYPE,
                                         _side_values parent.side_values%TYPE,
                                         _version parent.version%TYPE)
    RETURNS SETOF PARENT
    ROWS 1
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY INSERT INTO parent
        (natural_id, other_natural_id, state, value,
         side_values, version)
        VALUES (_natural_id, _other_natural_id, _state, _value,
                _side_values, _version)
        ON CONFLICT (natural_id) DO UPDATE
            SET (other_natural_id, state, value,
                 side_values, version)
                = (excluded.other_natural_id, excluded.state, excluded.value,
                   excluded.side_values, excluded.version)
        RETURNING *;
END;
$$;

-- Workaround issue in Spring Data with passing sets for ARRAY types in a procedure
CREATE OR REPLACE FUNCTION upsert_child(_natural_id child.natural_id%TYPE,
                                        _other_natural_id child.other_natural_id%TYPE,
                                        _parent_natural_id child.parent_natural_id%TYPE,
                                        _state child.state%TYPE,
                                        _at child.at%TYPE,
                                        _value child.value%TYPE,
                                        _default_side_values child.default_side_values%TYPE,
                                        _side_values child.side_values%TYPE,
                                        _version child.version%TYPE)
    RETURNS SETOF CHILD
    ROWS 1
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY INSERT INTO child
        (natural_id, other_natural_id, parent_natural_id,
         state, at, value,
         side_values,
         default_side_values, version)
        VALUES (_natural_id, _other_natural_id, _parent_natural_id,
                _state, _at, _value,
                _side_values,
                _default_side_values, _version)
        ON CONFLICT (natural_id) DO UPDATE
            SET (other_natural_id, parent_natural_id, state, at, value,
                 side_values,
                 default_side_values, version)
                = (excluded.other_natural_id, excluded.parent_natural_id,
                   excluded.state, excluded.at, excluded.value,
                   excluded.side_values,
                   excluded.default_side_values, excluded.version)
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
        RAISE 'Cannot change the natural key: %.%: NEW: %; OLD: %',
            TG_TABLE_SCHEMA, TG_TABLE_NAME, new, old;
    END IF;
    RETURN new;
END;
$$;

CREATE OR REPLACE FUNCTION insert_other_natural_id_audit_f()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
DECLARE
    now TIMESTAMP DEFAULT now();
BEGIN
    -- While running upsert do on conflict, Postgres fires both insert and
    -- update triggers.  Check for this case, and do not overwrite the
    -- existing audit columns.  When an existing row, the update trigger
    -- handles the rest
    PERFORM * FROM other WHERE natural_id = new.natural_id;
    IF FOUND THEN
        RETURN new;
    END IF;

    new.version := 1;
    new.created_at := now;
    new.updated_at := now;
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
    -- While running upsert do on conflict, Postgres fires both insert and
    -- update triggers.  Check for this case, and do not overwrite the
    -- existing audit columns.  When an existing row, the update trigger
    -- handles the rest
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
    -- While running upsert do on conflict, Postgres fires both insert and
    -- update triggers.  Check for this case, and do not overwrite the
    -- existing audit columns.  When an existing row, the update trigger
    -- handles the rest
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
    old_hash := md5(CAST((old.*) AS TEXT));
    new_hash := md5(CAST((new.*) AS TEXT));

    IF (old_hash = new_hash) THEN
        RETURN NULL; -- Bail out of update if no changes
    END IF;

    IF (new.version <> old.version) THEN
        RAISE 'Outdated: %.%: NEW: %; OLD: %', TG_TABLE_SCHEMA, TG_TABLE_NAME, new, old;
    END IF;

    new.version := old.version + 1;
    new.updated_at = now();
    RETURN new;
END;
$$;

CREATE OR REPLACE FUNCTION update_other_update_parent_f()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE parent
    SET updated_at = now() + INTERVAL '1 millisecond'
    WHERE other_natural_id IN (old.natural_id, new.natural_id);

    RETURN new;
END;
$$;

CREATE OR REPLACE FUNCTION update_other_update_child_f()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE child
    SET updated_at = now() + INTERVAL '1 millisecond'
    WHERE other_natural_id IN (old.natural_id, new.natural_id);

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
    SET updated_at = now() + INTERVAL '1 millisecond'
    WHERE natural_id = new.parent_natural_id;

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
    SET updated_at = now() + INTERVAL '1 millisecond'
    WHERE natural_id IN (old.parent_natural_id, new.parent_natural_id);

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
    SET updated_at = now() + INTERVAL '1 millisecond'
    WHERE natural_id = old.parent_natural_id;

    RETURN old;
END;
$$;

CREATE TRIGGER a_update_other_natural_id_immutable_natural_key_t
    BEFORE UPDATE
    ON other
    FOR EACH ROW
EXECUTE PROCEDURE update_immutable_natural_key_f();

CREATE TRIGGER b_update_other_update_parent_t
    BEFORE UPDATE
    ON other
    FOR EACH ROW
EXECUTE PROCEDURE update_other_update_parent_f();

CREATE TRIGGER c_update_other_update_child_t
    BEFORE UPDATE
    ON other
    FOR EACH ROW
EXECUTE PROCEDURE update_other_update_child_f();

CREATE TRIGGER d_insert_other_natural_id_audit_t
    BEFORE INSERT
    ON other
    FOR EACH ROW
EXECUTE PROCEDURE insert_other_natural_id_audit_f();

CREATE TRIGGER d_update_other_natural_id_audit_t
    BEFORE UPDATE
    ON other
    FOR EACH ROW
EXECUTE PROCEDURE update_audit_f();

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
