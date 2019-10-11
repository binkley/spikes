INSERT INTO parent
    (natural_id)
VALUES
    ('a');

SELECT upsert_parent('b', NULL, NULL); -- Batch update
SELECT upsert_parent('b', NULL, 1); -- Batch update

UPDATE parent -- Should fail with custom DB error message
   SET natural_id = 'c'
 WHERE natural_id = 'a';

SELECT upsert_parent('a', NULL, 1); -- Nothing happens
SELECT upsert_parent('a', 'BARBARBAR', 1); -- Something happens


SELECT upsert_parent('a', NULL, 0); -- Fail
SELECT upsert_parent('a', NULL, 1); -- Fail
SELECT upsert_parent('a', NULL, 3); -- Fail
SELECT upsert_parent('a', NULL, 2); -- Pass


UPDATE parent -- Should fail: avoid stale updates
   SET version = version - 1
 WHERE natural_id = 'a';

UPDATE parent -- Should be ignored: No change; audit columns should remain the same
   SET version = version
 WHERE natural_id = 'a';

UPDATE parent
   SET value = 'FOO!'
 WHERE natural_id = 'a';

UPDATE parent -- Should be ignored: No change; audit columns should remain the same
   SET value = 'FOO!'
 WHERE natural_id = 'a';

----

INSERT INTO child(natural_id, subchildren) -- unassigned
VALUES
    ('p', '[]');

SELECT upsert_child('q', NULL, NULL, '[]', NULL); -- Batch update


UPDATE child -- assign
   SET parent_natural_id = 'a'
 WHERE natural_id = 'p';

UPDATE child -- reassign
   SET parent_natural_id = 'b'
 WHERE natural_id = 'p';

UPDATE child
   SET value = 'BAR!'
 WHERE natural_id = 'p';

UPDATE child -- Should be ignored: No change; audit columns should remain the same
   SET subchildren = '[]'
 WHERE natural_id = 'p';

UPDATE child
   SET subchildren = '[
     "x",
     "y",
     "z"
   ]'
 WHERE natural_id = 'p';

DELETE -- and unassign
  FROM child
 WHERE natural_id = 'p';


SELECT json_array_length(subchildren)
  FROM child
 WHERE natural_id = 'p';
