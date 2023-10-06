DROP TABLE IF EXISTS t_flight;
CREATE TABLE t_flight
(
    id INTEGER NOT NULL,
    userid INTEGER NOT NULL,
    takeoff VARCHAR(128) NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user
(
    id   INTEGER      NOT NULL,
    name VARCHAR(128) NOT NULL,
    PRIMARY KEY (id)
);


