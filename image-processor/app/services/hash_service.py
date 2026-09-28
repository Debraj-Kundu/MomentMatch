from PIL import Image
import imagehash

class HashService:
    @staticmethod
    def perceptual_hash(image: Image.Image) -> str:
        return str(imagehash.phash(image))

    @staticmethod
    def average_hash(image: Image.Image) -> str:
        return str(imagehash.average_hash(image))
    