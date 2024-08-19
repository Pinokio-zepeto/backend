![PinokioLogo](https://github.com/user-attachments/assets/521896e7-2afa-4da6-a5df-dfe230f48df1)

## 개요

노인분들이 키오스크를 잘 사용하지 못하는 것을 보고 사용을 도와드리는 서비스가 있다면 좋겠다는 생각이 들어 기획하게 되었습니다.

프랜차이즈 매장에서 기존에 있던 상담원 직원을 활용하여 노년층 고객들에게 화상 통화를 통해 도움을 드리는 서비스입니다.

## 문서

Notion : [링크](https://fluffy-smell-11f.notion.site/SSAFY-PJT-5cf6c9977a6c460a98d2f81f9ae9db14)

요구사항명세서, 기능명세서, API연동규격서, GanttChart : [링크](https://docs.google.com/spreadsheets/d/16FjF0Qtb4-MWAu4Q0hWI4wvSguh9GAdlqrgbwzFRMfc/edit?gid=9229699#gid=9229699)

## 팀원 및 역할

| 이름   | 역할  | 내용                      |
| ------ | ----- | ------------------------- |
| 김준우 | FE    | 프론트엔드 개발           |
| 문재성 | FE    | 프론트엔드 개발, Openvidu |
| 이상무 | FE    | 프론트엔드 개발           |
| 전용수 | BE    | 백엔드 개발               |
| 정연서 | BE    | 백앤드 개발, Openvidu     |
| 최장우 | Infra | CI/CD, Openvidu           |

## 기술 스택

### 백엔드(Spring Boot, Gradle)

- Spring Boot: 3.2.7
- Spring Dependency Management: 1.1.5
- Google Protobuf Plugin: 0.8.19
- Java Language Version: 21
- OpenVidu Java Client: 2.20.0
- LiveKit Server: 0.5.11
- Springdoc OpenAPI UI: 2.0.2
- JSON Library: 20230227
- Spring Cloud AWS: 2.2.6.RELEASE
- JJWT API: 0.11.5
- JJWT Impl: 0.11.4
- JJWT Jackson: 0.11.4
- gRPC Netty Shaded: 1.57.2
- gRPC Protobuf: 1.57.2
- gRPC Stub: 1.57.2
- Apache Commons Math: 3.6.1
- javax.annotation API: 1.3.2
- Protobuf Java: 3.23.4
- Protobuf Java Util: 3.23.4
- Apache HttpClient 5: 5.2.1
- Protoc-gen-grpc-java: 1.57.
- Protobuf Compiler (protoc): 3.23.4

### 프론트엔드(React, Redux)

- React: 18.2.0
- Axios: 1.7.2
- Prettier: 2.8.8
- redux: 5.0.1
- redux-persist: 6.0.0

## 아키텍처

## ERD

![Pinkio ERD다이어그램](https://github.com/user-attachments/assets/ac73acdc-8e1d-40ef-91b6-cc74ee9a9e83)
