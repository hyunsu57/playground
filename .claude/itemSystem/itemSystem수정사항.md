# 게임 아이템 거래 상품 등록/목록 API  구현

## 1. 기술 스택
    - spring boot 3.x 이상
    - java 17+ 이상
    - JPA/hibernate 
    - DB : h2

## 2. 데이터 모델

| 필드명        | 설명     | 타입        | 비고                             |
|------------|--------|-----------|--------------------------------|
| id         | 상품고유Id | int       | PK, Auto Increment             |
| server     | 서버명    | varchar   | 예: "라엘08", "오르페 통합거래소          |
| sellerName | 판매자 이름 | varchar   |                                |
| itemType   | 상품 종류  | varchar   | GAME_MONEY, ITEM, ACCOUNT, ETC |
| title      | 상품명    | varchar   |                                |
| price      | 거래가격   | decimal   | 원 단위                           |
| quantity   | 판매수량   | int       | 숫자                             |
| createdAt  | 등록일시   | timestamp |                                |


## 3. 기능 요구사항

### 3.1 상품 목록 조회 API GET /api/items
- 필수 구현:
    - 페이지네이션 (page, size 파라미터)
    - 검색 기능 (상품명 키워드 검색)
    - 필터링 1개 이상:
    - 상품 종류(itemType) 필터
    - 서버(server) 필터
    - 가격 범위(minPrice ~ maxPrice) 필터 중 택1 이상
    - 정렬 옵션 (등록일순, 가격순)
    - 로딩/에러/빈 결과에 대한 응답 처리
```json
{
"content": [
{
"id": 1,
"server": "라엘08",
"sellerName": "아리",
"itemType": "GAME_MONEY",
"title": "다야 팝니다 필요하신만큼 신청해주세요",
"price": 25470,
"quantity": 3000,
"createdAt": "2025-01-15T10:30:00"
}
],
"page": 0,
"size": 20,
"totalElements": 150,
"totalPages": 8
}
```

### 3.2 상품 등록 API POST /api/items
- 필수 구현:
    - 요청 필드 검증 (Validation)
    - 필수 필드 누락 체크
    - 수량/가격은 양수만 허용
    - 상품 종류는 정의된 enum 값만 허용
    - 검증 실패 시 400 Bad Request + 상세 에러 메시지
    - 등록 성공 시 201 Created + 생성된 상품 정보 반환
```json
{
  "server": "라엘08",
  "sellerName": "아리",
  "itemType": "GAME_MONEY",
  "title": "1,000,000다이아 일괄 판매합니다.",
  "price": 100000,
  "quantity": 1000000
}
```

### 3.3 상품 수정
  - 필수 구현
    - 수정되는 컬럼은 제목, 상품 종류, 가격, 판매수량
### 3.4 상품 삭제
  - 필수 구현
    - id만 받아서 상품삭제

### 4. 문법
  - 요청의 경우 record 문법으로 할것
  - 검증 하는 부분을 구현하여 목록 조회, 상품 등록등의 검증이 필요
  - ItemConst에서 enum클래스를 활용할것 상품종류와 정렬을 통해 목록조회 및 상품이 등록될때 해당 enum클래스의 value가 들어갈것


## 5. 추가 기능
- 목록 조회 캐싱(spring cache등)
- 동시성 처리 (동일 상품 동시 수정시 처리방안)


