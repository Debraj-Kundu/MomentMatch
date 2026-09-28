from io import BytesIO
from dataclasses import dataclass
import numpy as np
from PIL import Image


@dataclass(slots=True)
class LoadedImage:
    bytes: BytesIO
    pil: Image.Image
    rgb: np.ndarray
    bgr: np.ndarray