package de.nexusbay.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Optional MariaDB connection pool for Phase 1+.
 * Currently prepared but only activated when database.enabled = true in config.yml.
 */
public final class DatabaseService {

    private final NexusBay plugin;
    private final Logger log;
    private HikariDataSource dataSource;

    public DatabaseService(NexusBay plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
    }

    public boolean enable() {
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.getBoolean("database.enabled", false)) {
            log.info("Database disabled – using YAML storage.");
            return false;
        }

        try {
            HikariConfig hc = new HikariConfig();
            String host = cfg.getString("database.host", "localhost");
            int port = cfg.getInt("database.port", 3306);
            String name = cfg.getString("database.name", "nexusbay");
            hc.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + name + "?useSSL=false&allowPublicKeyRetrieval=true");
            hc.setUsername(cfg.getString("database.user", "nexusbay"));
            hc.setPassword(cfg.getString("database.password", ""));
            hc.setMaximumPoolSize(cfg.getInt("database.pool-size", 10));
            hc.setPoolName("NexusBay-Hikari");
            hc.addDataSourceProperty("cachePrepStmts", "true");
            hc.addDataSourceProperty("prepStmtCacheSize", "250");
            hc.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(hc);

            try (Connection c = dataSource.getConnection()) {
                log.info("MariaDB connection established.");
            }
            return true;
        } catch (Exception e) {
            log.severe("Could not connect to MariaDB: " + e.getMessage());
            log.severe("Falling back to YAML storage.");
            if (dataSource != null) {
                dataSource.close();
                dataSource = null;
            }
            return false;
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) throw new SQLException("Database not enabled");
        return dataSource.getConnection();
    }

    public boolean isEnabled() {
        return dataSource != null && !dataSource.isClosed();
    }

    public void disable() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("Database pool closed.");
        }
    }
}
