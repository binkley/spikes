INSERT INTO parent
    (natural_id)
VALUES
    ('a');

INSERT INTO parent
    (natural_id)
VALUES
    ('b');

UPDATE parent -- Should fail with custom DB error message
   SET natural_id = 'c'
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

INSERT INTO child(natural_id) -- unassigned
VALUES
    ('p');

UPDATE child -- assign
   SET parent_id = (SELECT id FROM parent WHERE natural_id = 'a') -- ID is 1
 WHERE natural_id = 'p';

UPDATE child -- reassign
   SET parent_id = (SELECT id FROM parent WHERE natural_id = 'b') -- ID is 2
 WHERE natural_id = 'p';

UPDATE child
   SET value = 'BAR!'
 WHERE natural_id = 'p';

DELETE -- and unassign
  FROM child
 WHERE natural_id = 'p';
