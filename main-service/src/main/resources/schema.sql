-- DROP TABLE IF EXISTS users;


CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(512) NOT NULL,
  CONSTRAINT pk_user PRIMARY KEY (id),
  CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(255) NOT NULL,
  CONSTRAINT pk_category PRIMARY KEY (id),
  CONSTRAINT UQ_CATEGORY_NAME UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS events (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    title VARCHAR(100) NOT NULL,
    annotation TEXT,
    category_id BIGINT,
    created_on TIMESTAMP without time zone,
    description TEXT,
    event_date TIMESTAMP without time zone,
    initiator_id BIGINT,
    latitude DOUBLE,
    longitude DOUBLE,
    paid BOOLEAN,
    participant_limit INTEGER,
    published_on TIMESTAMP without time zone,
    request_moderation BOOLEAN,
    event_state VARCHAR(15),
    views INTEGER,
    CONSTRAINT pk_events PRIMARY KEY (id),
    CONSTRAINT fk_events_to_users FOREIGN KEY(initiator_id) REFERENCES users(id),
    CONSTRAINT fk_events_to_categories FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS requests (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    event_id BIGINT,
    created TIMESTAMP without time zone,
    requester_id BIGINT,
    status VARCHAR(10),
    CONSTRAINT pk_requests PRIMARY KEY (id),
    CONSTRAINT fk_requests_to_users FOREIGN KEY(requester_id) REFERENCES users(id),
    CONSTRAINT fk_requests_to_events FOREIGN KEY(event_id) REFERENCES events(id)
);

CREATE TABLE IF NOT EXISTS compilations (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    title VARCHAR(255),
    pinned BOOLEAN,
    CONSTRAINT pk_requests PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS compilation_event (
    compilation_id BIGINT,
    event_id BIGINT,
    PRIMARY KEY(compilation_id, event_id),
    FOREIGN KEY(compilation_id) REFERENCES compilations(id),
    FOREIGN KEY(event_id) REFERENCES events(id)
);