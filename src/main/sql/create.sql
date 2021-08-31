CREATE TABLE Counters(
     key INT primary key,
     value INT
);

insert into Counters (key, value)
select i, 0 from generate_series(1, 100) as t(i);