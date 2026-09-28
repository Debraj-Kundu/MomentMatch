import { memo } from 'react';

type AlbumGalleryProps = {
  images: { mediaId: string; downloadUrl: string }[];
};

export const AlbumGallery = memo(function AlbumGallery({ images }: AlbumGalleryProps) {
  return (
    <div className="image-grid">
      {images.map((image, index) => (
        <div
          key={image.mediaId}
          className="result-image"
          role="img"
          aria-label={`Gallery image ${index + 1}`}
          style={{
            background: `url("${image.downloadUrl}") center / cover no-repeat`,
          }}
        >
        </div>
      ))}
    </div>
  );
});

