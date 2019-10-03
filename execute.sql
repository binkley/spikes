INSERT INTO parent
    (abc)
VALUES
    ('a');

UPDATE parent
   SET abc = 'b'
 WHERE abc = 'a';

INSERT INTO child(pqr)
VALUES
    ('p');

UPDATE child
   SET parent_id = (SELECT id FROM parent WHERE abc = 'b')
 WHERE pqr = 'p';

DELETE
  FROM child
 WHERE pqr = 'p';



SELECT *
  FROM parent;

SELECT *
  FROM child;
