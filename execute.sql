INSERT INTO parent
    (natural_id)
VALUES
    ('a');

UPDATE parent -- Should fail with custom DB error message
   SET natural_id = 'b'
 WHERE natural_id = 'a';

UPDATE parent -- Should fail: avoid stale updates
    SET version = version - 1
WHERE natural_id = 'a';

UPDATE parent
   SET value = 'FOO!'
 WHERE natural_id = 'a';

UPDATE parent -- Should be ignored: No change; audit columns should remain the same
   SET value = 'FOO!'
 WHERE natural_id = 'a';

INSERT INTO child(natural_id)
VALUES
    ('p');

UPDATE child
   SET parent_id = (SELECT id FROM parent WHERE natural_id = 'a') -- ID is 1
 WHERE natural_id = 'p';

UPDATE child
   SET value = 'BAR!'
 WHERE natural_id = 'p';

DELETE
  FROM child
 WHERE natural_id = 'p';
