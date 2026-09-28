import json
import inspect
import sys
import logging
from typing import Any, Optional
import numpy as np
from pydantic import BaseModel


def _default_serializer(obj: Any) -> Any:
    if isinstance(obj, BaseModel):
        return obj.model_dump(mode="json")
    if isinstance(obj, Exception):
        return str(obj)
    if isinstance(obj, (np.generic, np.integer, np.floating)):
        return obj.item()
    if isinstance(obj, np.ndarray):
        return obj.tolist()
    if hasattr(obj, "__dict__"):
        return obj.__dict__
    return str(obj)


def log(
    log_message: str,
    payload: Optional[Any] = None,
    err: Optional[Any] = None,
    function_name: Optional[str] = None
) -> None:
    """
    Prints a JSON formatted log line containing:
      - function_name: Name of caller function (auto-detected if None)
      - log_message: Main log message
      - payload: Optional payload data (if provided)
      - err: Optional error details (if provided)
    """
    if not function_name:
        frame = inspect.currentframe().f_back
        if frame:
            function_name = frame.f_code.co_name
        else:
            function_name = "unknown"

    entry = {
        "function_name": function_name,
        "log_message": str(log_message),
    }

    if payload is not None:
        entry["payload"] = payload

    if err is not None:
        entry["err"] = str(err) if isinstance(err, Exception) else err

    print(json.dumps(entry, default=_default_serializer), flush=True)


class JSONFormatter(logging.Formatter):
    def format(self, record: logging.LogRecord) -> str:
        entry = {
            "function_name": record.funcName,
            "log_message": record.getMessage(),
        }
        if hasattr(record, "payload") and record.payload is not None:
            entry["payload"] = record.payload
        if record.exc_info:
            entry["err"] = self.formatException(record.exc_info)
        elif hasattr(record, "err") and record.err is not None:
            entry["err"] = str(record.err)
            
        return json.dumps(entry, default=_default_serializer)


def get_logger(name: str = "app") -> logging.Logger:
    logger = logging.getLogger(name)
    if not logger.handlers:
        handler = logging.StreamHandler(sys.stdout)
        handler.setFormatter(JSONFormatter())
        logger.addHandler(handler)
        logger.setLevel(logging.INFO)
    return logger
