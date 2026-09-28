import torch
from app.core.clip import clip_model, clip_processor
from app.core.ImageEmbedding import ImageEmbedding


class ImageEmbeddingService:

    @staticmethod
    def generate(image) -> ImageEmbedding:

        print("STEP 1: received image", type(image))

        inputs = clip_processor(
            images=image,
            return_tensors="pt"
        )

        print("STEP 2: processor output:", inputs.keys())

        with torch.no_grad():
            outputs = clip_model.get_image_features(
                pixel_values=inputs["pixel_values"]
            )

        print("STEP 3: model output:", type(outputs))
        print("STEP 4: pooler output:", outputs.pooler_output.shape)

        image_embeddings = outputs.pooler_output

        image_embeddings = image_embeddings / image_embeddings.norm(
            p=2,
            dim=-1,
            keepdim=True
        )

        return ImageEmbedding(
            embedding=image_embeddings[0].cpu().tolist()
        )

    @staticmethod
    def text_embedding(text: str) -> list[float]:

        inputs = clip_processor(
            text=[text],
            return_tensors="pt",
            padding=True
        )

        with torch.no_grad():
            outputs = clip_model.get_text_features(
                input_ids=inputs["input_ids"],
                attention_mask=inputs["attention_mask"]
            )

        text_embeddings = outputs.pooler_output

        text_embeddings = text_embeddings / text_embeddings.norm(
            p=2,
            dim=-1,
            keepdim=True
        )

        return text_embeddings[0].cpu().tolist()