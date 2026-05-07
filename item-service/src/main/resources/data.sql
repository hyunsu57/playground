-- 개발 환경 샘플 데이터 (dev 프로파일 시 자동 실행)
-- ddl-auto: create-drop 이후 INSERT 되므로 테이블이 먼저 생성된 상태여야 함

INSERT INTO items (server, seller_name, item_type, title, price, quantity, version, created_at)
VALUES
  ('라엘08', '아리', 'GAME_MONEY', '다야 팝니다 필요하신만큼 신청해주세요', 25470, 3000, 0, CURRENT_TIMESTAMP),
  ('오르페 통합거래소', '전사왕', 'ITEM', '강화 +15 대검 팝니다 급처', 1500000, 1, 0, CURRENT_TIMESTAMP),
  ('라엘08', '마법사99', 'ACCOUNT', '레벨 85 마법사 계정 판매합니다', 500000, 1, 0, CURRENT_TIMESTAMP),
  ('오르페 통합거래소', '아리', 'GAME_MONEY', '루비 대량 판매합니다', 100000, 50000, 0, CURRENT_TIMESTAMP),
  ('라엘08', '장비왕', 'ITEM', '전설 방어구 일괄 판매', 3000000, 1, 0, CURRENT_TIMESTAMP),
  ('라엘08', '거래소봇', 'ETC', '인챈트 재료 묶음 판매', 15000, 100, 0, CURRENT_TIMESTAMP),
  ('오르페 통합거래소', '포션왕', 'ITEM', '희귀 아이템 세트 저렴하게 팝니다', 750000, 3, 0, CURRENT_TIMESTAMP),
  ('라엘08', '초보거래', 'GAME_MONEY', '소량 다야 판매', 5000, 500, 0, CURRENT_TIMESTAMP);
