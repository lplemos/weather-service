-- Create cities table
CREATE TABLE IF NOT EXISTS cities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    timezone VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create weather_data table
CREATE TABLE IF NOT EXISTS weather_data (
    id BIGSERIAL PRIMARY KEY,
    city_id BIGINT NOT NULL REFERENCES cities(id),
    temperature DOUBLE PRECISION,
    feels_like DOUBLE PRECISION,
    humidity INTEGER,
    pressure DOUBLE PRECISION,
    wind_speed DOUBLE PRECISION,
    wind_direction VARCHAR(50),
    description VARCHAR(255),
    icon VARCHAR(100),
    visibility INTEGER,
    uv_index INTEGER,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    source VARCHAR(50) NOT NULL
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_weather_data_city_id ON weather_data(city_id);
CREATE INDEX IF NOT EXISTS idx_weather_data_timestamp ON weather_data(timestamp);
CREATE INDEX IF NOT EXISTS idx_weather_data_city_timestamp ON weather_data(city_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_cities_active ON cities(is_active); 