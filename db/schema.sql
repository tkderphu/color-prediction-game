CREATE DATABASE IF NOT EXISTS cgo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cgo;

-- Người dùng
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Trận đấu (mỗi lần ấn Bắt đầu sẽ tạo 1 match)
CREATE TABLE matches (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  room_owner VARCHAR(50) NOT NULL,
  started_at DATETIME NOT NULL,
  ended_at DATETIME NULL
);

-- Người chơi tham gia trận
CREATE TABLE match_players (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  match_id BIGINT NOT NULL,
  username VARCHAR(50) NOT NULL,
  total_score DECIMAL(6,2) DEFAULT 0,
  total_time_ms BIGINT DEFAULT 0,
  FOREIGN KEY (match_id) REFERENCES matches(id)
);

-- Vòng đấu (1..15)
CREATE TABLE rounds (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  match_id BIGINT NOT NULL,
  round_no INT NOT NULL,
  level ENUM('EASY','MEDIUM','HARD') NOT NULL,
  colors_json VARCHAR(255) NOT NULL,       -- ví dụ: ["RED","BLUE","GREEN"]
  show_ms INT NOT NULL,                     -- 10000 / 7000 / 5000
  countdown_ms INT NOT NULL,                -- 5000 + show_ms
  sent_at DATETIME NOT NULL,
  FOREIGN KEY (match_id) REFERENCES matches(id)
);

-- Kết quả vòng của từng người chơi
CREATE TABLE round_results (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  round_id BIGINT NOT NULL,
  username VARCHAR(50) NOT NULL,
  selected_json VARCHAR(255) NULL,          -- ví dụ: ["RED","GREEN","BLUE"]
  score DECIMAL(6,2) DEFAULT 0,
  time_ms BIGINT NOT NULL,                  -- nếu không gửi: = countdown_ms
  sent_at DATETIME NULL,                    -- NULL nếu không gửi
  FOREIGN KEY (round_id) REFERENCES rounds(id)
);

-- Lịch sử/leaderboard tiện truy vấn
CREATE INDEX idx_match_players_match ON match_players(match_id);
CREATE INDEX idx_rounds_match ON rounds(match_id);
CREATE INDEX idx_round_results_round ON round_results(round_id);

-- Tạo user test (mật khẩu hash băm sẵn sẽ set sau bằng app)
INSERT INTO users (username, password_hash) VALUES ('alice', '$2a$10$placeholder'), ('bob', '$2a$10$placeholder');
