CREATE INDEX idx_media_gallery_by_album
    ON media (album_id, image_type, upload_status)
    WHERE is_deleted = FALSE;
