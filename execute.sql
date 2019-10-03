INSERT INTO parent
    (natural_id)
VALUES
    ('a');

UPDATE parent
   SET natural_id = 'b'
 WHERE natural_id = 'a';

INSERT INTO child(natural_id)
VALUES
    ('p');

UPDATE child
   SET parent_id = (SELECT id FROM parent WHERE natural_id = 'b')
 WHERE natural_id = 'p';

DELETE
  FROM child
 WHERE natural_id = 'p';
