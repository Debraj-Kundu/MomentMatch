import httpx
from app.core.logger import log

class WebhookService:
    def __init__(self):
        self._client = httpx.AsyncClient(timeout=10)

    async def notify(self, url: str, status: str, error: str = None):
        try: 
            payload = {
                "status": status,
                "error": error
            }

            response = await self._client.post(url, json=payload)
            response.raise_for_status()
        except Exception as e:
            log("Error sending webhook", payload={"url": url, "status": status}, err=e)
            return None