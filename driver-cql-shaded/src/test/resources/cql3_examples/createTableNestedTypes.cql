CREATE TABLE cycling.race_winners (
   race_name text,
   race_position int,
   cyclist_name List<Set<fullname>>,
   PRIMARY KEY (race_name, race_position));
