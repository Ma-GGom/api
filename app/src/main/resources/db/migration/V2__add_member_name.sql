-- 회원 이름 (가입 후 설정 화면에서 선택 입력)
ALTER TABLE member ADD COLUMN IF NOT EXISTS name VARCHAR(20);
