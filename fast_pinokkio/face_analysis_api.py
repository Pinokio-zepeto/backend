from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
import numpy as np
import cv2
import base64
import json
import uuid
import os
from dotenv import load_dotenv
import logging
from typing import List
from cryptography.hazmat.primitives import padding
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.backends import default_backend
from insightface.app import FaceAnalysis
from pydantic import BaseModel

# 환경 변수 로드
load_dotenv()

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()

class ImageData(BaseModel):
    images: List[str]

class AnalysisResult(BaseModel):
    age: int
    gender: str
    is_face: bool
    encrypted_embedding: str

# InsightFace 얼굴 분석기 초기화
face_analyzer = FaceAnalysis(providers=['CPUExecutionProvider'])
face_analyzer.prepare(ctx_id=0, det_size=(640, 640))

def preprocess_face(frame):
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    equalized = cv2.equalizeHist(gray)
    blurred = cv2.GaussianBlur(equalized, (5, 5), 0)
    processed = cv2.cvtColor(blurred, cv2.COLOR_GRAY2BGR)
    return processed

def get_closest_face(faces):
    if not faces:
        return None
    return max(faces, key=lambda face: (face.bbox[2] - face.bbox[0]) * (face.bbox[3] - face.bbox[1]))

@app.post("/analyze_faces")
async def analyze_faces(data: ImageData):
    request_id = str(uuid.uuid4())
    logger.info(f"Received request with {len(data.images)} images")

    best_result = None
    best_score = 0

    for i, base64_image in enumerate(data.images):
        try:
            image_data = base64.b64decode(base64_image)
            nparr = np.frombuffer(image_data, np.uint8)
            img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
            preprocessed_face = preprocess_face(img)
            faces = face_analyzer.get(preprocessed_face)
            closest_face = get_closest_face(faces)

            if closest_face and closest_face.det_score > best_score:
                age = closest_face.age
                gender = "Male" if closest_face.gender == 1 else "Female"
                embedding = closest_face.embedding

                # 임베딩 차원 로그 출력
                logger.info(f"Embedding shape: {np.shape(embedding)}")
                logger.info(f"Embedding dimensions: {len(embedding)}")

                if closest_face.det_score > 0.5:
                    encrypted_embedding = json.dumps(embedding.tolist())
                    logger.info("encrypted_embedding size: %d", len(encrypted_embedding))

                    best_result = AnalysisResult(
                        age=age,
                        gender=gender,
                        is_face=True,
                        encrypted_embedding=encrypted_embedding
                    )
                    best_score = closest_face.det_score
                    logger.info(f"Image {i+1} processed successfully: Age {age}, Gender {gender}, Score {best_score}")
                else:
                    logger.warning(f"Image {i+1}: Face not clearly analyzed")
            else:
                logger.warning(f"Image {i+1}: No face detected or low quality face")
        except Exception as e:
            logger.error(f"Error processing image {i+1}: {str(e)}")

    if best_result is None:
        raise HTTPException(status_code=400, detail="No valid faces detected in any of the images")

    logger.info(f"Best face analysis result selected. Returning result.")
    return JSONResponse(content={"result": best_result.dict()})

@app.get("/fast/health")
async def health_check():
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("face_analysis_api:app", host="0.0.0.0", port=8000)
