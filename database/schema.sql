-- NEXUS-BAY zentrale Datenbank (MariaDB / MySQL)
-- Wird vom Installer importiert.

CREATE DATABASE IF NOT EXISTS nexusbay
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE nexusbay;

-- Spielerprofile
CREATE TABLE IF NOT EXISTS players (
  uuid            CHAR(36)      PRIMARY KEY,
  username        VARCHAR(16)   NOT NULL,
  rank_name       VARCHAR(32)   NOT NULL DEFAULT 'SPIELER',
  coins           BIGINT        NOT NULL DEFAULT 250,
  level           INT           NOT NULL DEFAULT 1,
  xp              INT           NOT NULL DEFAULT 0,
  first_join      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_join       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  playtime_seconds BIGINT       NOT NULL DEFAULT 0,
  INDEX idx_username (username),
  INDEX idx_rank (rank_name)
) ENGINE=InnoDB;

-- Transaktionshistorie (Economy)
CREATE TABLE IF NOT EXISTS transactions (
  id              BIGINT        AUTO_INCREMENT PRIMARY KEY,
  from_uuid       CHAR(36)      NULL,
  to_uuid         CHAR(36)      NULL,
  amount          BIGINT        NOT NULL,
  reason          VARCHAR(128)  NOT NULL,
  created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_from (from_uuid),
  INDEX idx_to (to_uuid),
  INDEX idx_created (created_at)
) ENGINE=InnoDB;

-- Freunde
CREATE TABLE IF NOT EXISTS friendships (
  id              BIGINT        AUTO_INCREMENT PRIMARY KEY,
  player_uuid     CHAR(36)      NOT NULL,
  friend_uuid     CHAR(36)      NOT NULL,
  status          ENUM('pending','accepted','blocked') NOT NULL DEFAULT 'pending',
  created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_friendship (player_uuid, friend_uuid),
  INDEX idx_player (player_uuid),
  INDEX idx_friend (friend_uuid)
) ENGINE=InnoDB;

-- Clans
CREATE TABLE IF NOT EXISTS clans (
  id              INT           AUTO_INCREMENT PRIMARY KEY,
  name            VARCHAR(24)   NOT NULL UNIQUE,
  tag             VARCHAR(8)    NOT NULL UNIQUE,
  owner_uuid      CHAR(36)      NOT NULL,
  created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  description     VARCHAR(256)  DEFAULT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS clan_members (
  clan_id         INT           NOT NULL,
  player_uuid     CHAR(36)      NOT NULL,
  role            ENUM('member','officer','leader') NOT NULL DEFAULT 'member',
  joined_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (clan_id, player_uuid),
  FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Moderationshistorie
CREATE TABLE IF NOT EXISTS moderation_logs (
  id              BIGINT        AUTO_INCREMENT PRIMARY KEY,
  target_uuid     CHAR(36)      NOT NULL,
  staff_uuid      CHAR(36)      NOT NULL,
  action          VARCHAR(32)   NOT NULL,
  reason          VARCHAR(512)  DEFAULT NULL,
  duration_seconds INT          DEFAULT NULL,
  created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_target (target_uuid),
  INDEX idx_staff (staff_uuid)
) ENGINE=InnoDB;

-- Ranghistorie
CREATE TABLE IF NOT EXISTS rank_history (
  id              BIGINT        AUTO_INCREMENT PRIMARY KEY,
  player_uuid     CHAR(36)      NOT NULL,
  old_rank        VARCHAR(32)   NOT NULL,
  new_rank        VARCHAR(32)   NOT NULL,
  changed_by      CHAR(36)      DEFAULT NULL,
  created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_player (player_uuid)
) ENGINE=InnoDB;
