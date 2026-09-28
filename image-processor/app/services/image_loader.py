from app.core.LoadedImage import LoadedImage
from io import BytesIO

import cv2
import numpy as np
from PIL import Image



class ImageLoader:
    @staticmethod
    def load_pil_image(image_data: BytesIO) -> Image.Image:
        return Image.open(image_data)

    @staticmethod
    def load_numpy_rgb(image_data: BytesIO) -> np.ndarray:
        return np.array(Image.open(image_data))

    @staticmethod
    def load_opencv_bgr(image_data: BytesIO) -> np.ndarray:
        image = Image.open(image_data).convert("RGB")

        rgb = np.array(image)

        return cv2.cvtColor(rgb, cv2.COLOR_RGB2BGR)
    
    @staticmethod
    def load(image_data: bytes) -> LoadedImage:
        byte_image = BytesIO(image_data)
        
        pil = Image.open(byte_image).convert("RGB")

        rgb = np.asarray(pil)

        bgr = cv2.cvtColor(rgb, cv2.COLOR_RGB2BGR)

        return LoadedImage(
            bytes=byte_image,
            pil=pil,
            rgb=rgb,
            bgr=bgr
        )