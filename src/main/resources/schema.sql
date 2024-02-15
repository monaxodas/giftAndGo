CREATE TABLE IF NOT EXISTS request (
    id CHAR(36) PRIMARY KEY,
    uri VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    response_status VARCHAR(255) NOT NULL,
    ip_address VARCHAR(255) NOT NULL,
    country_code VARCHAR(255) NOT NULL,
    isp VARCHAR(255) NOT NULL,
    time_elapsed BIGINT NOT NULL,
    created_date TIMESTAMP NOT NULL
);