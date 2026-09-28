import { getToken } from "next-auth/jwt";
import { NextRequest, NextResponse } from "next/server";

const backendUrl = process.env.BACKEND_URL;

async function proxy(request: NextRequest, { params }: { params: Promise<{ path: string[] }> }) {
  if (!backendUrl) return NextResponse.json({ message: "Backend URL is not configured." }, { status: 500 });

  const token = await getToken({ req: request, secret: process.env.AUTH_SECRET });
  if (!token?.accessToken) return NextResponse.json({ message: "Unauthorized" }, { status: 401 });

  const { path } = await params;
  const url = new URL(`/api/v1/${path.join("/")}`, backendUrl);
  url.search = new URL(request.url).search;
  const body = ["GET", "HEAD"].includes(request.method) ? undefined : await request.arrayBuffer();
  const response = await fetch(url, {
    method: request.method,
    headers: {
      Authorization: `Bearer ${token.accessToken}`,
      ...(request.headers.get("content-type") ? { "Content-Type": request.headers.get("content-type")! } : {}),
    },
    body,
  });

  return new NextResponse(response.body, {
    status: response.status,
    headers: response.headers.get("content-type") ? { "Content-Type": response.headers.get("content-type")! } : undefined,
  });
}

export { proxy as GET, proxy as POST, proxy as PUT, proxy as PATCH, proxy as DELETE };
