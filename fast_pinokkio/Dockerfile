# 베이스 이미지로 Python 3.9 사용
FROM python:3.12

# 필요한 시스템 라이브러리 설치
RUN apt-get update && apt-get install -y \
    libgl1-mesa-glx \
    libglib2.0-0
# 작업 디렉토리 설정
WORKDIR /app

# 필요한 패키지 설치
COPY requirements.txt /app/requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# 애플리케이션 파일 복사
COPY . /app

# FastAPI 애플리케이션 실행
CMD ["uvicorn", "face_analysis_api:app", "--host", "0.0.0.0", "--port", "5000"]
