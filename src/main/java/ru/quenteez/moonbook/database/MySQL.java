package ru.quenteez.moonbook.database;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.quenteez.moonbook.MoonBook;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MySQL {

    @Getter
    private final HikariDataSource SQL = new HikariDataSource();
    private final JavaPlugin instance;
    @Getter
    private boolean isDisabled = false;

    /**
     Creates a new pool for MServerAPI via HikariCP, which allows database manipulation
     */
    public MySQL(MoonBook instance, String pluginName) {
        this.instance = instance;
        if (!this.checkValidates("host", "port", "database", "username", "password")) {
            this.isDisabled = true;
            return;
        }
        this.SQL.setPoolName(pluginName.toLowerCase() + "-hikari");
        this.SQL.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
        this.SQL.setMaximumPoolSize(instance.getConfig().getInt("database.pool-settings.maximum-pool-size"));
        this.SQL.setMinimumIdle(instance.getConfig().getInt("database.pool-settings.minimum-idle"));
        this.SQL.setMaxLifetime(instance.getConfig().getInt("database.pool-settings.maximum-lifetime"));
        this.SQL.setIdleTimeout(instance.getConfig().getInt("database.pool-settings.idle-timeout"));
        this.SQL.setConnectionTimeout(instance.getConfig().getInt("database.pool-settings.connection-timeout"));
        this.SQL.addDataSourceProperty("serverName", instance.getConfig().getString("database.host"));
        this.SQL.addDataSourceProperty("port", instance.getConfig().getInt("database.port"));
        this.SQL.addDataSourceProperty("databaseName", instance.getConfig().getString("database.database"));
        this.SQL.addDataSourceProperty("user", instance.getConfig().getString("database.username"));
        this.SQL.addDataSourceProperty("password", instance.getConfig().getString("database.password"));
        this.SQL.addDataSourceProperty("socketTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(30L)));
        this.SQL.addDataSourceProperty("useSSL", false);
        this.SQL.addDataSourceProperty("characterEncoding", "utf8");
        this.SQL.addDataSourceProperty("verifyServerCertificate", false);
    }

    /**
     Executes a query to the database, returns nothing in response.
     */
    public void execute(String sql) {
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.instance, () -> {
            PreparedStatement ps = null;
            try {
                try (Connection connection = this.SQL.getConnection()) {
                    ps = connection.prepareStatement(sql);
                    ps.execute();
                }
                this.close(ps);
            }
            catch (SQLException ex) {
                try {
                    Bukkit.getLogger().warning(this.instance.getDescription().getName() + " SQL Failure | " + ex.getErrorCode());
                    Bukkit.getLogger().warning("QUERY:   " + sql);
                    this.close(ps);
                }
                catch (Throwable throwable) {
                    this.close(ps);
                    throw throwable;
                }
            }
        });
    }

    /**
     Creates a table in the database.
     */
    @SneakyThrows
    public int createTable(String tableName, String... fields) {
        PreparedStatement ps;
        try (Connection c = SQL.getConnection()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + Arrays.toString(fields).replaceAll("\\]|\\[", "") + ");";
            ps = c.prepareStatement(sql);
            return ps.executeUpdate(sql);
        }
    }

    private void close(PreparedStatement ps) {
        try {
            if (ps != null && !ps.isClosed()) {
                ps.close();
            }
        }
        catch (SQLException ex) {
            Bukkit.getLogger().warning("");
            Bukkit.getLogger().warning(this.instance.getDescription().getName() + " SQL Failure | " + ex.getErrorCode());
            Bukkit.getLogger().warning("PreparedStatement failed to close!");
            Bukkit.getLogger().warning(ex.getMessage());
            Bukkit.getLogger().warning("");
        }
    }

    /**
     Checks the parameters for connecting to the database.
     If one of the parameters is not set, it will cause an exception.
     */
    public boolean checkValidates(String ... parameters) {
        for (String param : parameters) {
            if (this.instance.getConfig().get("database." + param) != null) continue;
            Bukkit.getConsoleSender().sendMessage("    §eWarnings");
            Bukkit.getConsoleSender().sendMessage("    §7You must specify the data to connect ");
            Bukkit.getConsoleSender().sendMessage("    §7to the database in the configuration.");
            Bukkit.getConsoleSender().sendMessage("    §7'" + param + "' return null");
            Bukkit.getConsoleSender().sendMessage("    §4The database is not connected.");
            return false;
        }
        return true;
    }

    private final synchronized String format(String f, Object... v) {
        return String.format(Locale.ENGLISH, f, v);
    }

    private synchronized ResultSet query(String query) throws SQLException {
        PreparedStatement statement = SQL.getConnection().prepareStatement(query);
        return statement.executeQuery();
    }

    /**
     Executes a query to the database and returns a List of Strings
     */
    @SneakyThrows
    public synchronized List<String> getStringList(String sql, String ch, Object... v) {
        ArrayList<String> result = new ArrayList<>();
        try {
            ResultSet rs = this.query(this.format(sql, v));

            while(rs.next()) {
                result.add(rs.getString(ch));
            }
        } catch (SQLException var6) {
            Bukkit.getLogger().warning("MySQL Error: " + var6.getMessage());
            var6.printStackTrace();
        }
        SQL.getConnection().close();
        return result;
    }
}
