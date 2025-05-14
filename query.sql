-- CREATE DATABASE purgo;
-- USE purgo;

-- [users] 사용자 정보 테이블
CREATE TABLE users (
    userId INT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(20) NOT NULL,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    pw VARCHAR(255) NOT NULL,
    profileImage VARCHAR(255),
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- [posts] 게시글 테이블
CREATE TABLE posts (
    postId INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content VARCHAR(1000),
    count INT DEFAULT 0,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(userId)
);

-- [comments] 댓글 테이블
CREATE TABLE comments (
    commentId INT AUTO_INCREMENT PRIMARY KEY,
    postId INT NOT NULL,
    userId INT NOT NULL,
    content VARCHAR(1000),
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (postId) REFERENCES posts(postId),
    FOREIGN KEY (userId) REFERENCES users(userId)
);

-- [badwordLogs] 비속어 필터링 기록 테이블
CREATE TABLE badwordLogs (
    logId INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    postId INT NULL,
    commentId INT NULL,
    originalWord VARCHAR(50) NOT NULL,
    filteredWord VARCHAR(50) NOT NULL,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(userId),
    FOREIGN KEY (postId) REFERENCES posts(postId) ON DELETE SET NULL,
    FOREIGN KEY (commentId) REFERENCES comments(commentId) ON DELETE SET NULL
);

-- [penaltyCounts] 사용자 비속어 누적 통계
CREATE TABLE penaltyCounts (
    userId INT PRIMARY KEY,
    penaltyCount INT DEFAULT 0,
    lastUpdate DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(userId)
);

-- [limits] 이용 제한 테이블
CREATE TABLE limits (
    userId INT PRIMARY KEY,
    startDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    endDate DATETIME,
    isActive BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (userId) REFERENCES users(userId)
);