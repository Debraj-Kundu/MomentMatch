import { memo } from "react";
import { Sparkles, Check, ArrowUp } from "lucide-react";

type Message = {
  id: string | number;
  role: "user" | "assistant";
  text: string;
  images?: string[];
};

export const MessageList = memo(function MessageList({
  messages,
}: {
  messages: Message[];
}) {
  return (
    <>
      {messages.map((message) => (
        <div className={`message-row ${message.role}`} key={message.id}>
          <div className="message-avatar">
            {message.role === "user" ? "AM" : <Sparkles size={14} />}
          </div>
          <div className="message-body">
            {message.images && (
              <div className="image-grid">
                {message.images.map((image, index) => (
                  <div
                    className="result-image"
                    style={{
                      background: image.startsWith("http")
                        ? `url("${image}") center / cover no-repeat`
                        : image,
                    }}
                    key={`${image}-${index}`}
                  >
                    <span>
                      {message.role === "assistant" && <Check size={13} />}
                    </span>
                  </div>
                ))}
              </div>
            )}
            {message.role === "assistant" && message.images && (
              <div className="result-meta">
                <span>
                  <Check size={13} /> {message.images.length} matches
                </span>
                <button>
                  View all <ArrowUp size={13} />
                </button>
              </div>
            )}
            <p>{message.text}</p>
          </div>
        </div>
      ))}
    </>
  );
});
