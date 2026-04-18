-- 修复 note_tag_rel 表结构
-- 如果表不存在则创建
CREATE TABLE IF NOT EXISTS note_tag_rel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    note_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    UNIQUE KEY uk_note_tag (note_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 如果表已存在但没有 id 列，添加 id 列
-- ALTER TABLE note_tag_rel ADD COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY FIRST;
