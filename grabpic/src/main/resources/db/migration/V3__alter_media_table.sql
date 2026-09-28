
CREATE TYPE image_type AS ENUM (
    'PRIMARY',
    'COVER',
    'GALLERY',
    'PROFILE'
);

ALTER TABLE media
        ADD COLUMN image_type image_type;