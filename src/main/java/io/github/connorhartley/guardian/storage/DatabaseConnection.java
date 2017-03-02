/*
 * MIT License
 *
 * Copyright (c) 2017 Connor Hartley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.connorhartley.guardian.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Optional;

public class DatabaseConnection {

    private final String driver;
    private final String address;
    private final String port;
    private final String database;
    private final String username;
    private final String password;

    private HikariConfig hikariConfig = new HikariConfig();

    private HikariDataSource dataSource;
    private boolean online;

    public DatabaseConnection(String driver, String address, String port,
                              String database, String username, String password) {
        this.driver = driver;
        this.address = address;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;

        Optional<HikariDataSource> temporaryDataSource = this.createDataSource();
        if (temporaryDataSource.isPresent()) {
            this.dataSource = temporaryDataSource.get();
            this.online = true;
        } else {
            this.online = false;
        }
    }

    private Optional<HikariDataSource> createDataSource() {
        switch (this.driver) {
            case "mysql": {
                this.hikariConfig.setPoolName("GuardianMySQLPool");
                this.hikariConfig.setJdbcUrl("jdbc:mysql://" + this.address + ":" + this.port +
                            "/" + this.database);
                this.hikariConfig.setUsername(this.username);
                this.hikariConfig.setPassword(this.password);
                this.hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
                this.hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
                this.hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
                this.hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                return Optional.of(new HikariDataSource(this.hikariConfig));
            }
            case "sqlite": {
                this.hikariConfig.setPoolName("GuardianSQLitePool");
                this.hikariConfig.setDriverClassName("org.sqlite.JDBC");
                this.hikariConfig.setJdbcUrl("jdbc:sqlite:" + this.address + "/" + this.database + ".db");
                this.hikariConfig.setConnectionTestQuery("SELECT 1");
                this.hikariConfig.setMaxLifetime(60000);
                this.hikariConfig.setIdleTimeout(45000);
                this.hikariConfig.setMaximumPoolSize(50);
            }
            default:
                return Optional.empty();
        }
    }

    public boolean isEnabled() {
        return this.online;
    }

    public String getDriver() {
        return this.driver;
    }

    public String getAddress() {
        return this.address;
    }

    public String getPort() {
        return this.port;
    }

    public String getDatabase() {
        return this.database;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public HikariDataSource getDataSource() {
        return this.dataSource;
    }

}
