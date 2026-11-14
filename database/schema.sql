-- NEXUS-BAY MariaDB Schema
-- Character set and engine for all tables
SET NAMES utf8mb4;
SET time_zone = '+00:00';

CREATE DATABASE IF NOT EXISTS nexusbay
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE nexusbay;

-- Players (shared across the network)
CREATE TABLE IF NOT EXISTS players (
  uuid            CHAR(36)      NOT NULL PRIMARY KEY,
  name            VARCHAR(16)   NOT NULL,
  level           INT           NOT NULL DEFAULT 1,
  xp              BIGINT        NOT NULL DEFAULT 0,
  coins           BIGINT        NOT NULL DEFAULT 250,
  crew_id         INT           NULL,
  first_join      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_seen       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_players_name (name),
  INDEX idx_players_crew (crew_id)
) ENGINE=InnoDB;

-- Islands
CREATE TABLE IF NOT EXISTS islands (
  id              INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name            VARCHAR(32)   NOT NULL UNIQUE,
  owner_uuid      CHAR(36)      NOT NULL,
  world           VARCHAR(64)   NOT NULL DEFAULT 'world',
  center_x        INT           NOT NULL,
  center_z        INT           NOT NULL,
  radius          INT           NOT NULL DEFAULT 42,
  created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_islands_owner (owner_uuid),
  CONSTRAINT fk_islands_owner FOREIGN KEY (owner_uuid) REFERENCES players(uuid) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Economy log (anti-dupe / audit)
CREATE TABLE IF NOT EXISTS economy_log (
  id              BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  from_uuid       CHAR(36)      NULL,
  to_uuid         CHAR(36)      NULL,
  amount          BIGINT        NOT NULL,
  reason          VARCHAR(128)  NOT NULL,
  created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_economy_from (from_uuid),
  INDEX idx_economy_to (to_uuid)
) ENGINE=InnoDB;

-- Crews
CREATE TABLE IF NOT EXISTS crews (
  id              INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name            VARCHAR(24)   NOT NULL UNIQUE,
  tag             VARCHAR(6)    NULL,
  leader_uuid     CHAR(36)      NOT NULL,
  level           INT           NOT NULL DEFAULT 1,
  xp              BIGINT        NOT NULL DEFAULT 0,
  bank            BIGINT        NOT NULL DEFAULT 0,
  created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_crews_leader (leader_uuid)
) ENGINE=InnoDB;

-- Crew members
CREATE TABLE IF NOT EXISTS crew_members (
  crew_id         INT           NOT NULL,
  player_uuid     CHAR(36)      NOT NULL,
  rank            VARCHAR(16)   NOT NULL DEFAULT 'member',
  joined_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (crew_id, player_uuid),
  CONSTRAINT fk_cm_crew FOREIGN KEY (crew_id) REFERENCES crews(id) ON DELETE CASCADE,
  CONSTRAINT fk_cm_player FOREIGN KEY (player_uuid) REFERENCES players(uuid) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Skill progress (weapon mastery + future trees)
CREATE TABLE IF NOT EXISTS skill_progress (
  player_uuid     CHAR(36)      NOT NULL,
  skill_tree      VARCHAR(32)   NOT NULL,  -- e.g. swords, pistols, harpoons, crew_raiders
  node_id         VARCHAR(48)   NOT NULL,
  unlocked_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (player_uuid, skill_tree, node_id),
  CONSTRAINT fk_skill_player FOREIGN KEY (player_uuid) REFERENCES players(uuid) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Quests / Logbook
CREATE TABLE IF NOT EXISTS quests (
  id              INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
  code            VARCHAR(48)   NOT NULL UNIQUE,
  type            ENUM('daily','weekly','crew','story','feat') NOT NULL,
  title           VARCHAR(128)  NOT NULL,
  description     TEXT          NULL,
  reward_coins    INT           NOT NULL DEFAULT 0,
  reward_xp       INT           NOT NULL DEFAULT 0,
  active          TINYINT(1)    NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS player_quests (
  player_uuid     CHAR(36)      NOT NULL,
  quest_id        INT           NOT NULL,
  progress        INT           NOT NULL DEFAULT 0,
  target          INT           NOT NULL DEFAULT 1,
  completed       TINYINT(1)    NOT NULL DEFAULT 0,
  completed_at    TIMESTAMP     NULL,
  PRIMARY KEY (player_uuid, quest_id),
  CONSTRAINT fk_pq_player FOREIGN KEY (player_uuid) REFERENCES players(uuid) ON DELETE CASCADE,
  CONSTRAINT fk_pq_quest FOREIGN KEY (quest_id) REFERENCES quests(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Simple server settings / key-value
CREATE TABLE IF NOT EXISTS settings (
  setting_key     VARCHAR(64)   NOT NULL PRIMARY KEY,
  setting_value   TEXT          NOT NULL
) ENGINE=InnoDB;
