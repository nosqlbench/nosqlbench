CREATE TABLE cycling.race_winners (
   race_name text,
   race_position int,
   cyclist_name FROZEN<fullname>,
   rider_code text,
   details text,
   PRIMARY KEY ((race_name, race_position), cyclist_name, rider_code)
);
