CREATE TABLE IF NOT EXISTS `variables` (
`key` VARCHAR(128) NOT NULL,
`value` TEXT NOT NULL,
`expiry` INTEGER DEFAULT 0)