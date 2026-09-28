"use client";

import { useEffect, useMemo, useState } from "react";
import { Client } from "@stomp/stompjs";
import { useQuery } from "@tanstack/react-query";
import { signOut, useSession } from "next-auth/react";
import SockJS from "sockjs-client";
import { LoginForm } from "@/components/auth/login-form";
import {
  createAlbumWithUploads,
  createMessage as createMessageRequest,
  createSession as createSessionRequest,
  getAlbum,
  getAlbumGallery,
  getAlbums,
  getMessages,
  getSessions,
  getUploadUrls,
  uploadFiles,
  type SessionMessage,
} from "@/lib/backend-api";
import {
  ArrowLeft,
  ArrowUp,
  Bell,
  Camera,
  ChevronDown,
  CloudUpload,
  FolderPlus,
  Image as ImageIcon,
  Layers3,
  LogOut,
  Menu,
  MoreHorizontal,
  Plus,
  Search,
  Sparkles,
  X,
} from "lucide-react";
import dynamic from "next/dynamic";

const AlbumGallery = dynamic(
  () => import("@/components/album-gallery").then((mod) => mod.AlbumGallery),
  { ssr: false },
);
const MessageList = dynamic(
  () => import("@/components/message-list").then((mod) => mod.MessageList),
  { ssr: false },
);

type Album = {
  id: string | number;
  name: string;
  count: number;
  color: string;
  active?: boolean;
};
type Session = {
  id: string | number;
  albumId: string | number;
  title: string;
  date: string;
  color: "green" | "orange" | "blue";
};
type Message = {
  id: string | number;
  role: "user" | "assistant";
  text: string;
  images?: string[];
  createdAt?: string;
};

function toChatMessage(message: SessionMessage): Message {
  return {
    id: message.id,
    role: message.role === "USER" ? "user" : "assistant",
    text: message.content,
    images: message.media_attachments?.map((attachment) => attachment.download_url),
    createdAt: message.created_at,
  };
}

function mergeMessages(current: Message[], incoming: Message[]): Message[] {
  const messagesById = new Map(current.map((message) => [String(message.id), message]));
  incoming.forEach((message) => messagesById.set(String(message.id), message));

  return [...messagesById.values()].sort((left, right) => {
    if (!left.createdAt || !right.createdAt) return 0;
    return Date.parse(left.createdAt) - Date.parse(right.createdAt);
  });
}

type UploadUrlsResponse = Awaited<ReturnType<typeof getUploadUrls>>;

const starterAlbums: Album[] = [
  {
    id: 1,
    name: "Summer in Portugal",
    count: 248,
    color: "peach",
    active: true,
  },
  { id: 2, name: "New York City", count: 164, color: "blue" },
  { id: 3, name: "Family & friends", count: 92, color: "yellow" },
  { id: 4, name: "Weekend hikes", count: 56, color: "mint" },
];

const suggestedImages = [
  {
    src: "linear-gradient(145deg,#f2c8a8 0%,#f6e1c8 44%,#7c9ba0 45%,#234d57 100%)",
    label: "Coastal morning",
  },
  {
    src: "linear-gradient(145deg,#b8d9d4 0%,#d5ede7 46%,#eda87e 47%,#ad624b 100%)",
    label: "Old town street",
  },
  {
    src: "linear-gradient(145deg,#f4d5a8 0%,#ffe8bd 48%,#9bb6b2 49%,#3b6c6b 100%)",
    label: "Golden hour",
  },
  {
    src: "linear-gradient(145deg,#a6c8d9 0%,#d8ecf1 50%,#d59a77 51%,#75483b 100%)",
    label: "Blue doors",
  },
  {
    src: "linear-gradient(145deg,#cadfc9 0%,#e7f0dc 43%,#dfad7d 44%,#7a5b4f 100%)",
    label: "Garden walk",
  },
  {
    src: "linear-gradient(145deg,#e9c9bd 0%,#f5e2d6 50%,#9db9c8 51%,#4d6876 100%)",
    label: "Ocean view",
  },
];

const initialMessages: Message[] = [
  {
    id: 1,
    role: "user",
    text: "Find photos from my trip that show a sunset near the water.",
  },
  {
    id: 2,
    role: "assistant",
    text: "I found 6 photos that match your search. They all have warm sunset light and water in the scene.",
    images: suggestedImages.map((image) => image.src),
  },
];

const starterSessions: Session[] = [
  {
    id: 1,
    albumId: 1,
    title: "Sunset by the water",
    date: "Today, 4:32 PM",
    color: "green",
  },
  {
    id: 2,
    albumId: 1,
    title: "Places with blue doors",
    date: "Yesterday, 10:18 AM",
    color: "orange",
  },
  {
    id: 3,
    albumId: 1,
    title: "Our best food moments",
    date: "May 28, 2024",
    color: "blue",
  },
  {
    id: 4,
    albumId: 2,
    title: "City lights after dark",
    date: "June 02, 2024",
    color: "blue",
  },
  {
    id: 5,
    albumId: 2,
    title: "Coffee shops to remember",
    date: "May 30, 2024",
    color: "orange",
  },
  {
    id: 6,
    albumId: 3,
    title: "Everyone in one place",
    date: "May 24, 2024",
    color: "green",
  },
];

async function loadAlbums(): Promise<Album[]> {
  const colors = ["peach", "blue", "yellow", "mint"];
  const albums = await getAlbums();
  return albums.map((album, index) => ({
    id: album.id,
    name: album.name,
    count: album.totalMedia,
    color: colors[index % colors.length],
  }));
}

function SessionList({
  album,
  sessions,
  activeSessionId,
  onBack,
  onSelect,
  onCreate,
}: {
  album: Album;
  sessions: Session[];
  activeSessionId: string | number;
  onBack: () => void;
  onSelect: (session: Session) => void;
  onCreate: () => void;
}) {
  const albumSessions = sessions.filter(
    (session) => session.albumId === album.id,
  );

  return (
    <div className="session-browser">
      <button className="back-to-albums" onClick={onBack}>
        <ArrowLeft size={15} /> All albums
      </button>
      <div className="session-browser-title">
        <span className={`album-thumb ${album.color}`}>
          <ImageIcon size={14} />
        </span>
        <div>
          <strong>{album.name}</strong>
          <small>{album.count} photos</small>
        </div>
      </div>
      <div className="session-browser-label">
        <span>SESSIONS</span>
        <span>{albumSessions.length}</span>
      </div>
      <div className="session-browser-list">
        {albumSessions.length ? (
          albumSessions.map((session) => (
            <button
              className={`session-browser-item ${session.id === activeSessionId ? "selected" : ""}`}
              key={session.id}
              onClick={() => onSelect(session)}
            >
              <span className={`session-dot ${session.color}`} />
              <span>
                <strong>{session.title}</strong>
                <small>{session.date}</small>
              </span>
              <MoreHorizontal size={15} />
            </button>
          ))
        ) : (
          <div className="empty-sessions">
            <Sparkles size={18} />
            <p>No sessions yet</p>
            <small>Start a search to create the first one.</small>
          </div>
        )}
      </div>
      <button className="new-session" onClick={onCreate}>
        <Plus size={16} /> New session
      </button>
    </div>
  );
}

export default function Home() {
  const { data: session, status } = useSession();
  const [albums, setAlbums] = useState(starterAlbums);
  const [activeAlbum, setActiveAlbum] = useState(starterAlbums[0]);
  const [sessions, setSessions] = useState(starterSessions);
  const [activeSession, setActiveSession] = useState(starterSessions[0]);
  const [selectedAlbumId, setSelectedAlbumId] = useState<string | null>(null);
  const [showSessions, setShowSessions] = useState(false);
  const [messagesBySession, setMessagesBySession] = useState<
    Record<string, Message[]>
  >({ 1: initialMessages });
  const [query, setQuery] = useState("");
  const [showAlbumForm, setShowAlbumForm] = useState(false);
  const [albumName, setAlbumName] = useState("");
  const [albumFiles, setAlbumFiles] = useState<File[]>([]);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState("");
  const [uploads, setUploads] = useState<{ file: File; url: string }[]>([]);
  const [menuOpen, setMenuOpen] = useState(false);

  const messages = messagesBySession[String(activeSession.id)] ?? [];
  const albumSessions = sessions.filter(
    (session) => session.albumId === activeAlbum.id,
  );

  useEffect(() => {
    if (
      typeof activeSession.id === "string" &&
      !messagesBySession[activeSession.id]
    ) {
      getMessages(activeSession.id)
        .then((fetchedMessages) => {
          const processedMessages = fetchedMessages.map(toChatMessage);
          setMessagesBySession((current) => ({
            ...current,
            [activeSession.id]: mergeMessages(current[activeSession.id] ?? [], processedMessages),
          }));
        })
        .catch(console.error);
    }
  }, [activeSession.id, messagesBySession]);

  useEffect(() => {
    if (status !== "authenticated" || typeof activeSession.id !== "string") return;

    let isActive = true;
    let stompClient: Client | undefined;
    const sessionId = activeSession.id;

    async function connectToSession() {
      try {
        const tokenResponse = await fetch("/api/socket-token", { cache: "no-store" });
        if (!tokenResponse.ok) throw new Error("Unable to authenticate the chat connection.");
        const { accessToken } = (await tokenResponse.json()) as { accessToken: string };
        if (!isActive) return;

        const configuredBackendUrl = process.env.NEXT_PUBLIC_BACKEND_URL;
        const backendUrl = configuredBackendUrl ||
          (process.env.NODE_ENV === "development" ? "http://localhost:8081" : null);
        if (!backendUrl) throw new Error("NEXT_PUBLIC_BACKEND_URL must be configured for production.");
        stompClient = new Client({
          webSocketFactory: () => new SockJS(`${backendUrl.replace(/\/$/, "")}/ws`),
          connectHeaders: { Authorization: `Bearer ${accessToken}` },
          reconnectDelay: 5000,
          onConnect: () => {
            if (!isActive) return;

            stompClient?.subscribe(`/topic/sessions/${sessionId}/messages`, (frame) => {
              const message = toChatMessage(JSON.parse(frame.body) as SessionMessage);
              setMessagesBySession((current) => ({
                ...current,
                [sessionId]: mergeMessages(current[sessionId] ?? [], [message]),
              }));
            });

            getMessages(sessionId)
              .then((history) => {
                if (!isActive) return;
                setMessagesBySession((current) => ({
                  ...current,
                  [sessionId]: mergeMessages(current[sessionId] ?? [], history.map(toChatMessage)),
                }));
              })
              .catch(console.error);
          },
          onStompError: (frame) => console.error("Chat socket error:", frame.headers.message),
        });
        void stompClient.activate();
      } catch (error) {
        if (isActive) console.error("Unable to connect to chat updates:", error);
      }
    }

    void connectToSession();
    return () => {
      isActive = false;
      if (stompClient) void stompClient.deactivate();
    };
  }, [activeSession.id, status]);

  const albumsQuery = useQuery({
    queryKey: ["albums"],
    queryFn: loadAlbums,
    enabled: status === "authenticated",
  });
  const albumQuery = useQuery({
    queryKey: ["album", selectedAlbumId],
    queryFn: () => getAlbum(selectedAlbumId!),
    enabled: selectedAlbumId !== null,
  });
  const galleryQuery = useQuery({
    queryKey: ["album", selectedAlbumId, "gallery"],
    queryFn: () => getAlbumGallery(selectedAlbumId!),
    enabled: selectedAlbumId !== null,
    staleTime: 55 * 60 * 1000,
    gcTime: 60 * 60 * 1000,
    refetchOnWindowFocus: false,
  });
  const displayedAlbums = albumsQuery.data ?? albums;
  const totalPhotos = useMemo(
    () => displayedAlbums.reduce((total, album) => total + album.count, 0),
    [displayedAlbums],
  );

  const openAlbum = async (album: Album) => {
    setActiveAlbum(album);
    if (typeof album.id === "string") setSelectedAlbumId(album.id);
    const albumSessions =
      typeof album.id === "string"
        ? (await getSessions(album.id)).map(
            (session, index): Session => ({
              id: session.id,
              albumId: album.id,
              title: session.title,
              date: "Recent",
              color: ["green", "orange", "blue"][index % 3] as Session["color"],
            }),
          )
        : sessions.filter((session) => session.albumId === album.id);
    if (typeof album.id === "string")
      setSessions((current) => [
        ...current.filter((session) => session.albumId !== album.id),
        ...albumSessions,
      ]);
    if (albumSessions[0]) setActiveSession(albumSessions[0]);
    setShowSessions(true);
  };

  const createSession = async () => {
    if (typeof activeAlbum.id !== "string") return;
    const createdSession = await createSessionRequest(
      activeAlbum.id,
      "New search",
    );
    const nextSession: Session = {
      id: createdSession.id,
      albumId: activeAlbum.id,
      title: createdSession.title,
      date: "Just now",
      color: "green",
    };
    setSessions((current) => [nextSession, ...current]);
    setActiveSession(nextSession);
    setShowSessions(true);
  };

  const createAlbum = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!albumName.trim() || !albumFiles.length) return;
    setIsUploading(true);
    setUploadError("");
    try {
      const result = await createAlbumWithUploads(albumName.trim(), albumFiles);
      await uploadFiles(albumFiles, result.uploadUrls.responses);
      const nextAlbum = {
        id: result.albumId,
        name: albumName.trim(),
        count: albumFiles.length,
        color: "lilac",
      };
      const createdSession = await createSessionRequest(
        result.albumId,
        "New search",
      );
      const firstSession = {
        id: createdSession.id,
        albumId: nextAlbum.id,
        title: createdSession.title,
        date: "Just now",
        color: "green" as const,
      };
      setAlbums((current) => [nextAlbum, ...current]);
      setSessions((current) => [firstSession, ...current]);
      setActiveAlbum(nextAlbum);
      setSelectedAlbumId(result.albumId);
      setActiveSession(firstSession);
      setShowSessions(true);
      setAlbumName("");
      setAlbumFiles([]);
      setShowAlbumForm(false);
      await albumsQuery.refetch();
    } catch (error) {
      setUploadError(
        error instanceof Error ? error.message : "Unable to create the album.",
      );
    } finally {
      setIsUploading(false);
    }
  };

  const sendMessage = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!query.trim() && uploads.length === 0) return;
    const currentQuery =
      query.trim() || "Find similar photos to these uploads.";
    
    if (typeof activeSession.id === "string") {
      const activeUploads = [...uploads];
      setQuery("");
      setUploads([]);

      let mediaIds: string[] | undefined = undefined;
      let urlsResponse: UploadUrlsResponse | null = null;
      const files = activeUploads.map((u) => u.file);

      if (files.length > 0 && typeof activeAlbum.id === "string") {
        try {
          urlsResponse = await getUploadUrls(activeAlbum.id, files, 'PRIMARY');
          mediaIds = urlsResponse.responses.map((response) => response.mediaId);
        } catch (error) {
          console.error("Failed to initialize upload", error);
          return;
        }
      }

      const message = await createMessageRequest(
        activeSession.id,
        currentQuery,
        mediaIds
      );
      
      setMessagesBySession((current) => ({
        ...current,
        [String(activeSession.id)]: [
          ...(current[String(activeSession.id)] ?? []),
          {
            id: message.id,
            role: "user",
            text: message.content,
            images: activeUploads.map(u => u.url),
            createdAt: message.created_at,
          },
        ],
      }));

      // Start upload in background
      if (urlsResponse && files.length > 0) {
        uploadFiles(files, urlsResponse.responses).catch(error => {
          console.error("Background image upload failed:", error);
        });
      }

      return;
    }
    setMessagesBySession((current) => ({
      ...current,
      [String(activeSession.id)]: [
        ...(current[String(activeSession.id)] ?? []),
        {
          id: Date.now(),
          role: "user",
          text: currentQuery,
          images: uploads.length ? uploads.map(u => u.url) : undefined,
        },
        {
          id: Date.now() + 1,
          role: "assistant",
          text: "I found a few close matches in your album. Here are the most visually similar moments.",
          images: suggestedImages.slice(1, 5).map((image) => image.src),
        },
      ],
    }));
    if (!sessions.some((session) => session.id === activeSession.id)) {
      setSessions((current) => [activeSession, ...current]);
    }
    setQuery("");
    setUploads([]);
  };

  if (status !== "authenticated") return <LoginForm />;

  const userEmail = session.user?.email ?? "Account";
  const selectedAlbum = albumQuery.data;

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-top">
          <div className="brand">
            <span className="brand-mark">
              <Sparkles size={15} />
            </span>{" "}
            reprise
          </div>
          <button
            className="icon-button mobile-menu"
            onClick={() => setMenuOpen(!menuOpen)}
            aria-label="Toggle menu"
          >
            <Menu size={19} />
          </button>
        </div>
        {!showSessions ? (
          <>
            <div className="workspace-label">
              <span>YOUR LIBRARY</span>
              <button
                onClick={() => setShowAlbumForm(true)}
                aria-label="Create album"
              >
                <Plus size={17} />
              </button>
            </div>
            <div className={`album-list ${menuOpen ? "open" : ""}`}>
              {albumsQuery.isLoading ? (
                <div className="loading-line" />
              ) : (
                displayedAlbums.map((album) => (
                  <button
                    className={`album-item ${activeAlbum.id === album.id ? "selected" : ""}`}
                    key={album.id}
                    onClick={() => {
                      openAlbum(album);
                      setMenuOpen(false);
                    }}
                  >
                    <span className={`album-thumb ${album.color}`}>
                      <ImageIcon size={14} />
                    </span>
                    <span className="album-name">{album.name}</span>
                    <span className="album-count">{album.count}</span>
                  </button>
                ))
              )}
            </div>
            <button
              className="new-album"
              onClick={() => setShowAlbumForm(true)}
            >
              <span>
                <FolderPlus size={17} />
              </span>{" "}
              New album
            </button>
          </>
        ) : (
          <SessionList
            album={activeAlbum}
            sessions={sessions}
            activeSessionId={activeSession.id}
            onBack={() => setShowSessions(false)}
            onSelect={(session) => {
              setActiveSession(session);
              setShowSessions(true);
              setMenuOpen(false);
            }}
            onCreate={createSession}
          />
        )}
        <div className="sidebar-footer">
          <div className="storage">
            <div>
              <span>Library storage</span>
              <strong>
                2.4 GB <small>of 10 GB</small>
              </strong>
            </div>
            <div className="storage-bar">
              <i />
            </div>
          </div>
          <button className="profile-button">
            <span className="avatar">
              {userEmail.slice(0, 2).toUpperCase()}
            </span>
            <span>
              <strong>{userEmail}</strong>
              <small>Personal account</small>
            </span>
            <MoreHorizontal size={17} />
          </button>
        </div>
      </aside>
      <section className="chat-area">
        <header className="chat-header">
          <div>
            <p className="breadcrumb">
              <button onClick={() => setShowSessions(true)}>
                {selectedAlbum?.name ?? activeAlbum.name}
              </button>
              <ChevronDown size={13} />
            </p>
            <h2>{activeSession.title}</h2>
          </div>
          <div className="header-actions">
            <button className="header-icon" aria-label="Search">
              <Search size={18} />
            </button>
            <button
              className="header-icon notification"
              aria-label="Notifications"
            >
              <Bell size={18} />
              <i />
            </button>
            <button
              className="header-icon"
              aria-label="Log out"
              onClick={() => signOut({ callbackUrl: "/" })}
            >
              <LogOut size={18} />
            </button>
          </div>
        </header>
        <div className="conversation">
          <div className="conversation-intro">
            <div className="intro-icon">
              <Sparkles size={20} />
            </div>
            <p>Search your memories</p>
            <h1>What are you looking for?</h1>
            <span>
              Ask in your own words. Reprise will look through{" "}
              <b>{totalPhotos} photos</b> in {activeAlbum.name}.
            </span>
          </div>
          <div className="messages">
            {galleryQuery.data && <AlbumGallery images={galleryQuery.data} />}
            <MessageList messages={messages} />
          </div>
        </div>
        <form className="composer-wrap" onSubmit={sendMessage}>
          <div className="upload-preview">
            {uploads.map((upload, index) => (
              <div
                key={`${upload.url}-${index}`}
                className="upload-chip"
                style={{ backgroundImage: `url(${upload.url})` }}
              >
                <button
                  type="button"
                  onClick={() =>
                    setUploads((current) =>
                      current.filter((_, itemIndex) => itemIndex !== index),
                    )
                  }
                >
                  <X size={11} />
                </button>
              </div>
            ))}
          </div>
          <div className="composer">
            <label className="attach-button" aria-label="Upload photos">
              <CloudUpload size={19} />
              <input
                type="file"
                accept="image/*"
                multiple
                onChange={(event) =>
                  setUploads(
                    Array.from(event.target.files ?? []).map((file) => ({
                      file,
                      url: URL.createObjectURL(file),
                    })),
                  )
                }
              />
            </label>
            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Describe a memory, a place, or a feeling..."
            />
            <button className="send-button" type="submit" aria-label="Send">
              <ArrowUp size={18} />
            </button>
          </div>
          <div className="composer-hint">
            <span>
              <Camera size={13} /> You can also upload photos to find similar
              images
            </span>
            <span>Reprise can make mistakes</span>
          </div>
        </form>
      </section>
      <aside className="details-panel">
        <div className="details-heading">
          <span>ALBUM OVERVIEW</span>
          <button aria-label="More options">
            <MoreHorizontal size={18} />
          </button>
        </div>
        <div className="overview-card">
          <div className="overview-cover">
            <div className="cover-sun" />
            <div className="cover-hill" />
          </div>
          <div className="overview-title">
            <div>
              <h3>{activeAlbum.name}</h3>
              <p>Created April 2024</p>
            </div>
            <button>
              <MoreHorizontal size={17} />
            </button>
          </div>
          <div className="overview-stats">
            <div>
              <strong>{activeAlbum.count}</strong>
              <span>Photos</span>
            </div>
            <div>
              <strong>12</strong>
              <span>Sessions</span>
            </div>
            <div>
              <strong>84%</strong>
              <span>Organized</span>
            </div>
          </div>
        </div>
        <div className="recent-heading">
          <span>RECENT SESSIONS</span>
          <button>See all</button>
        </div>
        <div className="session-list">
          {albumSessions.map((session) => (
            <button
              className={`session-item ${session.id === activeSession.id ? "active" : ""}`}
              key={session.id}
              onClick={() => {
                setActiveSession(session);
                setShowSessions(true);
              }}
            >
              <span className={`session-dot ${session.color}`} />
              <span>
                <strong>{session.title}</strong>
                <small>{session.date}</small>
              </span>
              <MoreHorizontal size={16} />
            </button>
          ))}
        </div>
        <div className="tip-card">
          <Sparkles size={16} />
          <div>
            <strong>Try a different search</strong>
            <p>Ask about colors, people, or the feeling of a moment.</p>
          </div>
        </div>
      </aside>
      {showAlbumForm && (
        <div className="modal-backdrop" onClick={() => setShowAlbumForm(false)}>
          <form
            className="album-modal"
            onSubmit={createAlbum}
            onClick={(event) => event.stopPropagation()}
          >
            <button
              type="button"
              className="modal-close"
              onClick={() => setShowAlbumForm(false)}
            >
              <X size={18} />
            </button>
            <div className="modal-icon">
              <Layers3 size={20} />
            </div>
            <p className="eyebrow">new collection</p>
            <h2>Create an album</h2>
            <p>
              Give your memories a place to live. Add photos now to start
              searching.
            </p>
            <label>
              <span>Album name</span>
              <input
                autoFocus
                required
                value={albumName}
                onChange={(event) => setAlbumName(event.target.value)}
                placeholder="e.g. Japan, 2024"
              />
            </label>
            <label>
              <span>Photos</span>
              <input
                required
                type="file"
                accept="image/*"
                multiple
                onChange={(event) =>
                  setAlbumFiles(Array.from(event.target.files ?? []))
                }
              />
            </label>
            {albumFiles.length > 0 && (
              <p>
                {albumFiles.length} photo{albumFiles.length === 1 ? "" : "s"}{" "}
                selected
              </p>
            )}
            {uploadError && (
              <p className="auth-error" role="alert">
                {uploadError}
              </p>
            )}
            <button
              className="primary-button"
              type="submit"
              disabled={isUploading}
            >
              {isUploading ? "Uploading..." : "Create album"}{" "}
              <ArrowUp size={16} className="arrow-right" />
            </button>
          </form>
        </div>
      )}
    </main>
  );
}
