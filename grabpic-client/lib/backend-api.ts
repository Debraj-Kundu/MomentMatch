type PageResponse<T> = { content: T[]; totalElements: number };

export type Album = {
  id: string;
  name: string;
  totalMedia: number;
  processedMedia: number;
  albumStatus: string;
  createdAt?: string;
};

export type AlbumSession = { id: string; title: string };
export type GalleryImage = { mediaId: string; downloadUrl: string };
type UploadUrl = { mediaId: string; uploadUrl: string };
type AlbumInitResponse = { albumId: string; uploadUrls: { responses: UploadUrl[] } };

export type SessionMessage = {
  id: string;
  role: 'USER' | 'ASSISTANT';
  content: string;
  media_attachments?: { media_id: string; download_url: string }[];
  created_at?: string;
};

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`/api/backend/${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options?.headers },
  });
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Request failed.' }));
    throw new Error(error.message ?? 'Request failed.');
  }
  return response.json() as Promise<T>;
}

export const getAlbums = () => request<PageResponse<Album>>('album?page=0&size=100').then(({ content }) => content);

export const getAlbum = (albumId: string) => request<Album>(`album/${albumId}`);

export const getAlbumGallery = (albumId: string) => request<PageResponse<GalleryImage>>(`album/${albumId}/gallery?page=0&size=100`).then(({ content }) => content);

export const createAlbumWithUploads = (name: string, files: File[]) => request<AlbumInitResponse>('album/init', {
  method: 'POST',
  body: JSON.stringify({
    album: { name },
    uploads: {
      files: files.map((file) => ({
        filename: file.name,
        fileType: file.type || 'application/octet-stream',
        sizeBytes: file.size,
        imageTag: 'GALLERY',
      })),
    },
  }),
});

export const getUploadUrls = (albumId: string, files: File[], imageTag: 'PRIMARY' | 'GALLERY' = 'PRIMARY') => request<{ responses: UploadUrl[] }>('media/upload-url', {
  method: 'POST',
  body: JSON.stringify({
    files: files.map(file => ({
      filename: file.name,
      fileType: file.type || 'application/octet-stream',
      sizeBytes: file.size,
      albumId,
      imageTag
    }))
  })
});

export async function uploadFiles(files: File[], uploads: UploadUrl[]) {
  if (files.length !== uploads.length) throw new Error('Upload URLs did not match the selected files.');

  await Promise.all(uploads.map(async ({ uploadUrl }, index) => {
    const response = await fetch(uploadUrl, {
      method: 'PUT',
      headers: { 'Content-Type': files[index].type || 'application/octet-stream' },
      body: files[index],
    });
    if (!response.ok) throw new Error(`Failed to upload ${files[index].name}.`);
  }));
}

export const createAlbum = (name: string) => request<Album>('album', { method: 'POST', body: JSON.stringify({ name }) });

export const getSessions = (albumId: string) => request<PageResponse<AlbumSession>>(`album/${albumId}/sessions?page=0&size=100`).then(({ content }) => content);

export const createSession = (albumId: string, title: string) => request<AlbumSession>('session', { method: 'POST', body: JSON.stringify({ album_id: albumId, title }) });

export const getMessages = (sessionId: string) => request<SessionMessage[]>(`session/${sessionId}/messages/history`);

export const createMessage = (sessionId: string, content: string, mediaIds?: string[]) => request<SessionMessage>(`session/${sessionId}/messages`, { method: 'POST', body: JSON.stringify({ role: 'USER', content, media_ids: mediaIds }) });

