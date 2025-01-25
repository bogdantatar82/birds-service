CREATE TABLE birds (
    id uuid NOT NULL,
    "name" varchar(255) NOT NULL,
    color varchar(255) NULL,
    weight int4 NOT NULL,
    height int4 NOT NULL,
    created timestamp NOT NULL,
    modified timestamp NOT NULL,
    CONSTRAINT birds_pkey PRIMARY KEY (id)
);

CREATE TABLE sightings (
    id uuid NOT NULL,
    location varchar(255) NOT NULL,
    "datetime" timestamp NOT NULL,
    bird_id uuid NOT NULL,
    created timestamp NOT NULL,
    modified timestamp NOT NULL,
    CONSTRAINT sightings_pkey PRIMARY KEY (id)
);

ALTER TABLE ONLY sightings
    ADD CONSTRAINT sightings_bird_id_fkey FOREIGN KEY (bird_id) REFERENCES birds(id);
