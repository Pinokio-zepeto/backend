![PinokioLogo](https://github.com/user-attachments/assets/521896e7-2afa-4da6-a5df-dfe230f48df1)

## 주요 개발 내용(BE) 
### 0. 회원 등록
  - 3가지 고객 타입 (포스, 키오스크, 상담원) 등록 기능 구현
  - 이메일 기반, Naver SMPT를 통한 인증
  - 랜덤 인증번호 생성 후 Redis에5분간 캐싱해 이와 대조하는 방식
  
### 1. 키오스크
  - 수정 / 삭제 / 정보 반환
  - **거리 감지**:  
     아두이노 센서를 통해 고객의 거리를 측정하고, 일정 거리 내에 고객이 감지되면 사진을 촬영한 뒤 데이터를 처리    
  - **사진 촬영 및 전송**:
    감지된 고객의 사진을 OpenCV로 촬영하고, Base64로 인코딩한 후 Spring 서버에 gRPC로 전송  
  - **gRPC 통신**:   
    키오스크와 Spring 서버 간의 데이터 전송은 gRPC를 통해 비동기로 처리(거리 데이터와 사진 파일 전송)  

    **코드**
    
    ```python
    import serial  # 시리얼 통신 라이브러리
    import cv2  # OpenCV 사용
    import base64  # Base64 인코딩
    import grpc  # gRPC 통신
    import asyncio  # 비동기 처리
    import time  # 시간 처리
    import kiosk_pb2  # gRPC 프로토콜 버퍼
    import kiosk_pb2_grpc  # gRPC 서비스 정의
    
    # 아두이노로부터 거리 데이터를 받는 함수
    async def get_distance():
        if arduino:
            arduino.reset_input_buffer()
            arduino.write(b'GET_DISTANCE\\n')
            await asyncio.sleep(0.1)  # 데이터가 수신될 때까지 대기
            if arduino.in_waiting > 0:
                try:
                    response = arduino.readline().decode('utf-8').strip()
                    distance = float(response)  # 거리 데이터 파싱
                    return distance if distance >= 0 else None  # 유효한 값이면 반환
                except (ValueError, serial.SerialException) as e:
                    logger.error(f"Distance reading error: {e}")
        return None
    
    # 카메라로 사진을 촬영하는 함수
    def capture_image():
        cap = cv2.VideoCapture(0)  # 카메라 시작
        ret, frame = cap.read()  # 사진 촬영
        cap.release()  # 카메라 닫기
        if ret:
            _, buffer = cv2.imencode('.jpg', frame)  # JPEG로 인코딩
            return base64.b64encode(buffer).decode('utf-8')  # Base64 인코딩 후 반환
        return None
    
    # Spring 서버로 거리 데이터를 전송하는 함수
    async def send_distance_to_spring(stub):
        global measuring_distance
        last_distance = None
        stable_count = 0
    
        async def send_data(distance):
            try:
                response = stub.ReceiveDistanceData(kiosk_pb2.DistanceData(kiosk_id=KIOSK_ID, distance=distance))
                logger.info(f"Distance data sent successfully: {distance} cm")
            except grpc.RpcError as e:
                logger.error(f"Failed to send distance data: {e}")
    
        while True:
            if not measuring_distance:
                await asyncio.sleep(1)
                continue
    
            distance = await get_distance()
            if distance is not None and distance < 80:  # 80cm 이하에서 사용자 감지
                logger.info(f"User detected: {distance} cm")
                asyncio.create_task(send_data(distance))  # 비동기 전송
            await asyncio.sleep(0.1)  # 100ms 대기
    
    # Spring 서버로 이미지 데이터를 전송하는 함수
    def send_image_to_spring(stub):
        image = capture_image()  # 이미지 촬영
        if image:
            logger.info(f"Captured image size: {len(image)} bytes")
            stub.CaptureImages(kiosk_pb2.CaptureImagesRequest(images=[image]))  # gRPC로 이미지 전송
    
    ```
- **얼굴 식별** :    
    - 이미지 품질 관리:  
        얼굴 분석의 정확도를 높이기 위해 OpenCV로 전처리 과정을 거쳐 이미지 품질 개선.      
        이미지를 그레이스케일로 변환한 후 히스토그램 평활화와 가우시안 블러를 적용해 얼굴 인식 품질을 향상.  
        
      ```python
        # 얼굴 이미지 전처리 함수
        def preprocess_face(frame):
            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)  # 그레이스케일 변환
            equalized = cv2.equalizeHist(gray)  # 히스토그램 평활화
            blurred = cv2.GaussianBlur(equalized, (5, 5), 0)  # 가우시안 블러 적용
            return cv2.cvtColor(blurred, cv2.COLOR_GRAY2BGR)  # 다시 BGR로 변환
        
        ```
         
      
    - InsightFace를 이용한 얼굴 분석:  
           전처리된 이미지를 InsightFace를 통해 분석하여 얼굴의 나이, 성별, 임베딩 데이터를 추출. 감지된 얼굴 중 가장 신뢰도 높은 얼굴을 선택해 최종 결과로 반환.       
        ```python
          # InsightFace 얼굴 분석을 통한 가장 가까운 얼굴 선택
          def get_closest_face(faces):
              if not faces:
                  return None
              # 얼굴 영역의 크기를 기준으로 가장 큰 얼굴 선택
              return max(faces, key=lambda face: (face.bbox[2] - face.bbox[0]) * (face.bbox[3] - face.bbox[1]))
        ```
    
### 2. 포스
  - 키오스크 등록 / 키오스크 정보 조회

### 3. 상담원
  - 탈퇴
    
### 4. 상담
  - 상담 요청 / 요청 응답을 위한 WebSocket 통신
  - OpenVidu 세션을 활용한 상담 기능 (입장/퇴장 관리)
    
### 5. 상품 관리
  - 상품 CRUD
  - 상품 이미지 등록의 경우 Amazon S3에 업로드
  
### 6. 주문 관리 
  - 주문 생성 / 상태 변경 / 통계값 조회
  - 단건 주문 총 가격 Redis 캐싱 -> 매장별 총 판매액 정보 look-aside 패턴 적용
  - 매 일/주/월/년 스케줄러를 통한 판매 통계 DB 저장
    
### 7. 고객 관리
  - 키오스크 기기로부터 얻은 사진 정보를 FastAPI로 전송해 Face Embedding Vector 저장
  - 임베딩 정보를 이용해 기존 고객들과의 유사도 계산
  - 고객 정보 등록 및 수정

### 8. SSE를 이용한 데이터 전송
 - **SSE 연결 관리**:  
    - 클라이언트와의 SSE 연결을 관리하며, 다수의 클라이언트에 대해 비동기적으로 실시간 데이터 전송    
    - `SseEmitter`를 통해 클라이언트에 이벤트를 전송하며, 각 클라이언트는 연결 상태에 따라 이벤트를 수신    
  - **Keep-Alive 메시지 전송**:  
    - 일정 시간마다 서버에서 모든 클라이언트로 Keep-Alive 메시지를 전송하여 SSE 연결이 유지될 수 있게 함.  
  - **대기 상태 변경 이벤트 전송**:  
    - 클라이언트에게 대기 상태(예: 고객이 대기 중인지 여부)를 실시간으로 전달.
  - **얼굴 감지 결과 전송**:
    - 얼굴이 감지되었는지 여부를 클라이언트에 전송하여, 실시간으로 얼굴 감지 상태를 공유.
  - **얼굴 분석 결과 전송**:
    - 얼굴 분석 결과(나이, 성별, 얼굴 임베딩 정보)를 클라이언트로 전송하며, 고객 정보가 있는 경우 함께 전송.

```java
// 새로운 SSE 연결을 생성하는 메서드
public SseEmitter createEmitter() {
    log.info("Creating new SSE connection.");
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);  // 무제한 시간의 SSE 연결
    this.emitters.add(emitter);  // 연결된 클라이언트 리스트에 추가

    emitter.onCompletion(() -> {
        this.emitters.remove(emitter);
        log.info("SSE connection completed. Remaining emitters: {}", emitters.size());
    });

    emitter.onTimeout(() -> {
        this.emitters.remove(emitter);
        log.warn("SSE connection timed out. Remaining emitters: {}", emitters.size());
    });

    try {
        emitter.send(SseEmitter.event()
                .name("connect")
                .data("Connected successfully"));  // 클라이언트로 연결 성공 메시지 전송
        log.info("Connected successfully message sent to client.");
    } catch (IOException e) {
        log.error("Error sending connect event: {}", e.getMessage());
        emitter.completeWithError(e);
    }
    return emitter;
}

```


## 사용 기술 및 선택 이유
- **아두이노와 시리얼 통신**:
    - `serial` 라이브러리를 통해 아두이노 센서와 통신하여 실시간으로 거리 데이터를 수집합니다. 연결 안정성을 위해 재시도 로직을 구현했습니다.
- **OpenCV**:
    - 카메라 모듈을 통해 실시간으로 고객의 사진을 촬영하고, 효율적인 이미지 처리 및 압축을 위해 OpenCV를 사용했습니다. 촬영된 이미지는 Base64로 인코딩되어 네트워크 전송을 최적화했습니다.
- **gRPC**:
    - Spring 서버와 키오스크 간의 통신을 위해 gRPC를 사용했습니다. gRPC는 다음과 같은 이유로 선택되었습니다:
        1. **고성능**: 바이너리 기반 프로토콜로, 속도가 빠르고 네트워크 효율성이 뛰어납니다.
        2. **다중 언어 지원**: Spring 서버와 Python 기반 키오스크 사이에서 쉽게 통신할 수 있도록 다양한 언어 지원.
        3. **비동기 처리**: 비동기 방식으로 데이터 전송을 처리해 거리 측정과 사진 촬영이 동시에 이루어질 수 있으며, 서버로의 데이터 전송이 차질 없이 진행됩니다.
        
        gRPC의 성능 이점을 활용하여 거리 데이터와 사진을 빠르고 효율적으로 전송했습니다.

- **InsightFace (얼굴 분석 라이브러리)**:
    - InsightFace는 얼굴 인식 및 분석에 있어 높은 정확도와 성능을 자랑하는 라이브러리입니다. 얼굴의 나이, 성별, 임베딩 값을 효율적으로 추출하여 얼굴 인식 및 분석 작업을 수행합니다.
    - **정확도**: 얼굴 감지와 분석에서 높은 정확도를 제공하며, 다양한 조명, 각도에서도 우수한 성능을 발휘합니다.

- **FastAPI (비동기 처리의 이점)**:
    - FastAPI는 Python의 고성능 비동기 웹 프레임워크로, 비동기 방식으로 요청을 처리하여 병목 없이 빠르게 여러 얼굴 분석 요청을 처리할 수 있습니다. RESTful API 개발에서 높은 성능과 유연성을 제공하며, 동시에 다양한 클라이언트의 요청을 처리할 수 있습니다.

- **Redis**:
    - Refresh Token, 이메일 인증번호 저장 시 사용했습니다.
    - 판매액 통계 정보 조회 시 look-aside 패턴을 적용했습니다. [기술적 고민 #3](### 3. 브랜드 판매액 통계)
      
- **SSE (Server-Sent Events)**:
    - HTTP 기반의 양방향 통신 기술로, 서버에서 클라이언트로 지속적인 데이터 스트림을 전송할 수 있는 방법을 제공합니다.
    - **효율성**: 웹소켓보다 가볍고, 클라이언트가 데이터를 필요로 할 때만 서버가 전송하므로 서버 자원의 낭비를 줄일 수 있습니다.
    - **브라우저 호환성**: SSE는 브라우저에서 기본적으로 지원되어 추가적인 라이브러리 없이도 손쉽게 구현 가능합니다.

## 기술적 고민
### 1. 인가 처리
**loadUserByUsername 오버라이드**  
3가지 고객 타입(포스, 키오스크, 상담원)에서 각자 요구되는 필드 값이 명확하게 다르기 때문에 모두 별도의 테이블로 설계되었습니다. 스프링 시큐리티의 UserDetail의 오버라이딩 메소드, 그리고 JwtProvider를 커스텀 해서 분리된 유저 권한을 제공할 수 있었습니다. 이후에는 Security Config를 통해서 각 권한이 접근할 수 있는 RestAPI를 통제하도록 설정할 수 있었습니다.  

```java
 @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        log.info("입력 값: {}", input);

        // 입력 값에서 역할과 이메일 분리
        String inputRole = input.substring(0, 1);
        String email = input.substring(1);

        log.info("입력 역할: {}", inputRole);
        log.info("입력 이메일: {}", email);

        // 역할을 Enum으로 변환
        Role role;
        try {
            role = Role.valueOf(inputRole);
        } catch (IllegalArgumentException e) {
            log.error("잘못된 역할 입력: {}", inputRole, e);
            throw new UsernameNotFoundException("잘못된 역할 입력: " + inputRole, e);
        }

        log.info("변환된 역할: {}", role);

        // 역할에 따른 사용자 검색 및 반환
        switch (role) {
            case P:
                log.info("POS 사용자 검색 중...");
                return posRepository.findByEmail(email)
                        .map(pos -> {
                            log.info("POS 사용자 발견: {}", pos.getEmail());
                            return new CustomUserDetail(pos.getEmail(), pos.getPassword(), role.getValue());
                        })
                        .orElseThrow(() -> {
                            log.error("{} : POS 사용자 존재하지 않음", email);
                            return new UsernameNotFoundException(email + " : POS 사용자 존재하지 않음");
                        });

            case K:
                log.info("KIOSK 사용자 검색 중...");
                return kioskRepository.findByEmail(email)
                        .map(kiosk -> {
                            log.info("KIOSK 사용자 발견: {}", kiosk.getEmail());
                            return new CustomUserDetail(kiosk.getEmail(), kiosk.getPassword(), role.getValue());
                        })
                        .orElseThrow(() -> {
                            log.error("{} : KIOSK 사용자 존재하지 않음", email);
                            return new UsernameNotFoundException(email + " : KIOSK 사용자 존재하지 않음");
                        });

            case T:
                log.info("TELLER 사용자 검색 중...");
                return tellerRepository.findByEmail(email)
                        .map(teller -> {
                            log.info("TELLER 사용자 발견: {}", teller.getEmail());
                            return new CustomUserDetail(teller.getEmail(), teller.getPassword(), role.getValue());
                        })
                        .orElseThrow(() -> {
                            log.error("{} : TELLER 사용자 존재하지 않음", email);
                            return new UsernameNotFoundException(email + " : TELLER 사용자 존재하지 않음");
                        });

            default:
                log.error("알 수 없는 역할: {}", role);
                throw new UsernameNotFoundException(role + " : 알 수 없는 역할");
        }
    }
```


### 2. OpenVidu  
**WebSocket, OpenVidu**  
상담원과 고객이 최대 1:3까지 상담이 가능하다는 조건을 만족시켜야 했으며, 상담 세션 외부에서 입장 요청과 이에 대한 응답을 송수신할 수 있어야 했습니다. 오픈비두 세션 외부에서 상담원이라는 Role을 가진 유저와 송신하는 기능을 웹소켓을 통해 구현하였습니다. 이때, 상담원 또는 키오스크의 식별자를 KEY로 하고, 해당 사용자의 웹소켓을 VALUE로 하는 ConcurrentHashMap 으로 관리하였습니다. 또한, 여러 고객이 동시에 같은 방에 접근하거나 수정할 때 발생할 수 있는 레이스 컨디션을 방지하기 위해 각 방에 대한 ReentrantLock을 관리하여, 주요 메서드에서 해당 방의 Lock을 획득하고 작업을 수행한 후 반드시 Lock을 해제하도록 하였습니다.  

### 3. 브랜드 판매액 통계  
**look-aside 패턴 적용**  
점주가 관리하는 포스기는 같은 브랜드 지점들의 기간별 판매 금액 통계 정보를 제공합니다. 이 기능은 기간별 매출 통계액을 조회할때마다 반복적인 연산이 발생하는 것을 고려하여 Redis를 사용하게 되었습니다. 레디스는 스케줄링을 통해서 최근 30일간의 매장별 판매액 합산 정보를 가지는데, 30일 이내 정보는 레디스 내부에서 합산하고, 더 오랜 기간의 정보를 원하는 경우 DB탐색을 진행하는 look-aside 패턴을 캐시전략으로 사용하였습니다.

```java
/**
     * 단건 주문 총 가격을 redis 에 30일간 캐싱한다.
     * @param posId     포스 식별자
     * @param salePrice 주문 총 가격
     */
    private void updateSalesInRedis(UUID posId, long salePrice) {
        // Redis 키 생성
        String key = "pos:" + posId + ":sales";
        // 현재 시간 (밀리초)
        String currentDate = String.valueOf(System.currentTimeMillis());
        // 매출 항목 저장: {timestamp: salesAmount}
        redisUtil.hset(key, currentDate, String.valueOf(salePrice));
        // 만료 설정: 30일 후에 자동으로 삭제
        redisUtil.expire(key, 30 * 24 * 60 * 60); // 30일
    }

    /**
     * 특정 포스의 30일간 총 판매액을 반환한다.
     * @param posId 포스 식별자
     * @return 30일간의 총 판매액
     */
    public long getTotalSales(UUID posId) {
        //key 생성
        String key = "pos:" + posId + ":sales";
        // 모든 매출 항목 가져오기
        Map<String, String> salesMap = redisUtil.hgetAll(key);

        long totalSales = 0;
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<String, String> entry : salesMap.entrySet()) {
            long timestamp = Long.parseLong(entry.getKey());
            // 30일이 지난 항목은 제외
            if (currentTime - timestamp <= 30 * 24 * 60 * 60 * 1000L) {
                totalSales += Long.parseLong(entry.getValue());
            }
        }
        return totalSales;
    }

    /**
     * 같은 code_id를 가진 Pos 들의 30일 총 판매 평균액, Pos 의 수, 현재 Pos 의 등수를 반환한다.
     * @param posId 현재 Pos 의 식별자
     * @return PosStatisticsDto
     */
    public PosStatisticsResponse getPosStatistics(UUID posId) {
        // 현재 Pos 찾기
        Pos currentPos = posRepository.findById(posId).orElseThrow(() -> new IllegalArgumentException("Pos not found"));
        UUID codeId = currentPos.getCode().getId();

        // 같은 code_id를 가진 모든 Pos 찾기
        List<Pos> posList = posRepository.findByCodeId(codeId);

        // 각 Pos 의 30일 총 판매액 계산
        List<Long> totalSalesList = posList.stream()
                .map(pos -> getTotalSales(pos.getId()))
                .toList();

        // 전체 판매액
        long totalSales = totalSalesList.stream().mapToLong(Long::longValue).sum();
        // Pos 의 수
        long posCount = posList.size();
        // 평균 판매액 계산
        long averageSales = posCount > 0 ? totalSales / posCount : 0;

        // 현재 Pos 의 총 판매액
        long currentPosSales = getTotalSales(posId);
        // 현재 Pos 의 등수 계산
        long currentPosRank = totalSalesList.stream()
                // 현재 Pos 보다 판매액이 높은 수를 카운트
                .filter(sales -> sales > currentPosSales)
        // 현재 Pos 의 등수 (1부터 시작)
                .count() + 1;

        return new PosStatisticsResponse(averageSales, posCount, currentPosRank);
    }
```
### 4. 얼굴 인식  

**비동기 처리**:    
    - FastAPI의 비동기 처리 특성을 활용하여 얼굴 분석 요청을 효율적으로 처리하고, 동시에 다수의 요청을 병목 없이 지원했습니다.  
    
**에러 처리**:    
    - 이미지 처리 및 얼굴 분석 도중 발생할 수 있는 다양한 예외 상황에 대한 로깅 및 오류 처리를 구현했습니다. 각 이미지 처리 중 발생하는 오류는 상세한 로그로 기록되며, 클라이언트에는 적절한 오류 메시지와 함께 반환됩니다.  


```python
# 얼굴 분석 API 엔드포인트
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
                best_result = {"age": age, "gender": gender, "is_face": True}
                best_score = closest_face.det_score

                logger.info(f"Image {i+1} processed successfully: Age {age}, Gender {gender}, Score {best_score}")
            else:
                logger.warning(f"Image {i+1}: No face detected or low quality face")
        except Exception as e:
            logger.error(f"Error processing image {i+1}: {str(e)}")

    if best_result is None:
        raise HTTPException(status_code=400, detail="No valid faces detected in any of the images")

    logger.info(f"Best face analysis result selected. Returning result.")
    return JSONResponse(content={"result": best_result})
```
## 5.Spring SSE (Server-Sent Events)
**연결 관리**:  
    - 각 `SseEmitter`는 클라이언트의 연결 상태에 따라 수명 주기가 다르므로, 연결 완료 또는 타임아웃 시 `emitters` 리스트에서 해당 객체를 제거하여 자원 누수를 방지했습니다.  
  
**예외 처리 및 오류 복구**:  
    - 클라이언트에게 데이터를 전송하는 과정에서 발생할 수 있는 다양한 예외 상황(네트워크 오류 등)을 처리하기 위해 `IOException` 및 기타 예외를 잡아내고, 문제가 발생한 `SseEmitter`는 적절하게 종료 처리했습니다.  

```java
// 이벤트를 모든 클라이언트에 전송하는 메서드
private void sendEventToAll(String eventName, Map<String, Object> eventData) {
    List<SseEmitter> deadEmitters = new ArrayList<>();

    for (SseEmitter emitter : this.emitters) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(eventData));  // 이벤트 전송
        } catch (IOException e) {
            log.error("IOException while sending event '{}': {}", eventName, e.getMessage());
            deadEmitters.add(emitter);  // 전송 실패한 emitter는 리스트에서 제거
        }
    }

    if (!deadEmitters.isEmpty()) {
        this.emitters.removeAll(deadEmitters);  // 실패한 emitters 삭제
        log.warn("Removed {} dead emitters. Remaining emitters: {}", deadEmitters.size(), emitters.size());
    }
}

```
**스레드 안정성, 데이터 무결성을 위한 `CopyOnWriteArrayList`**:  
    - 여러 클라이언트의 SSE 연결을 관리하기 위해 `CopyOnWriteArrayList`를 사용하여 스레드 안정성을 보장했습니다. 여러 스레드가 동시에 접근하여 읽고 쓰는 상황에서도 데이터 무결성을 유지할 수 있습니다.    

        
**ScheduledExecutorService (Keep-Alive 구현)**:   
    - 서버와 클라이언트 간의 연결 유지를 위해 일정 주기마다 Keep-Alive 메시지를 전송하는 스케줄러를 실행합니다.  
    - 주기적으로 클라이언트로 상태 정보를 전송함으로써 연결이 유지되고 있는지 확인하고, 유지된 연결을 통해 얼굴 인식 결과, 분석 결과 등의 이벤트를 실시간으로 클라이언트에 전달할 수 있도록 구현했습니다.  

```java
// 주기적으로 Keep-Alive 메시지를 모든 클라이언트에 전송
private void sendKeepAlive() {
    log.debug("Sending keep-alive message to all connected clients.");
    Map<String, Object> keepAliveData = new HashMap<>();
    keepAliveData.put("status", "keep-alive");
    sendEventToAll("keepAlive", keepAliveData);  // 모든 클라이언트에 이벤트 전송
}

```
