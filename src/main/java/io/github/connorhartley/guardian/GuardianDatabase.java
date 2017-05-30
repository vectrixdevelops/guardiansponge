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
package io.github.connorhartley.guardian;

import io.github.connorhartley.guardian.storage.StorageProvider;
import io.github.connorhartley.guardian.storage.database.DatabaseQuery;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.service.sql.SqlService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public final class GuardianDatabase implements StorageProvider<Connection> {

    private Connection connection;

    private final Guardian plugin;
    private final String connectionPath;
    private final SqlService sqlService;

    public DatabaseQuery databaseVersionTable;
    public DatabaseQuery databasePunishmentTable;
    public DatabaseQuery databaseLocationTable;
    public DatabaseQuery databasePlayerTable;

    public GuardianDatabase(Guardian plugin, String connectionPath, SqlService sqlService) {
        this.plugin = plugin;
        this.connectionPath = connectionPath;
        this.sqlService = sqlService;
    }

    @Override
    public void create() {
        try {
            this.connection = this.sqlService.getDataSource(this.plugin, this.connectionPath).getConnection();

            this.databaseVersionTable = DatabaseQuery.builder(this.connection)
                    .append("CREATE TABLE IF NOT EXISTS GUARDIAN")
                    .append(StringUtils.join("(",
                            "GUARDIAN_VERSION varchar(24) NOT NULL, ",
                            "DATABASE_VERSION integer NOT NULL, ",
                            "SYNCHRONIZE_TIME timestamp NOT NULL, ",
                            "PUNISHMENT_TABLE varchar(24) NOT NULL, ",
                            "LOCATION_TABLE varchar(24) NOT NULL, ",
                            "PLAYER_TABLE varchar(24) NOT NULL, ",
                            "PRIMARY KEY(DATABASE_VERSION)"))
                    .append(")")
                    .build();

            this.databasePunishmentTable = DatabaseQuery.builder(this.connection)
                    .append("CREATE TABLE IF NOT EXISTS GUARDIAN_PUNISHMENT")
                    .append(StringUtils.join("(",
                            "ID integer NOT NULL, ",
                            "DATABASE_VERSION integer NOT NULL, ",
                            "PLAYER_UUID varchar(36) NOT NULL, ",
                            "PUNISHMENT_TYPE varchar(64) NOT NULL, ",
                            "PUNISHMENT_REASON varchar(1024) NOT NULL, ",
                            "PUNISHMENT_TIME timestamp NOT NULL, ",
                            "PUNISHMENT_PROBABILITY double precision NOT NULL, ",
                            "FOREIGN KEY(DATABASE_VERSION) REFERENCES GUARDIAN(DATABASE_VERSION), ",
                            "PRIMARY KEY(ID)"))
                    .append(")")
                    .build();

            this.databaseLocationTable = DatabaseQuery.builder(this.connection)
                    .append("CREATE TABLE IF NOT EXISTS GUARDIAN_LOCATION")
                    .append(StringUtils.join("(",
                            "PUNISHMENT_ID integer NOT NULL, ",
                            "DATABASE_VERSION integer NOT NULL, ",
                            "PUNISHMENT_ORDINAL integer NOT NULL, ",
                            "WORLD_UUID varchar(36) NOT NULL, ",
                            "X double precision NOT NULL, ",
                            "Y double precision NOT NULL, ",
                            "Z double precision NOT NULL, ",
                            "FOREIGN KEY(DATABASE_VERSION) REFERENCES GUARDIAN(DATABASE_VERSION), ",
                            "PRIMARY KEY(PUNISHMENT_ID, PUNISHMENT_ORDINAL)"))
                    .append(")")
                    .build();

            this.databasePlayerTable = DatabaseQuery.builder(this.connection)
                    .append("CREATE TABLE IF NOT EXISTS GUARDIAN_PLAYER")
                    .append(StringUtils.join("(",
                            "PUNISHMENT_ID integer NOT NULL, ",
                            "DATABASE_VERSION integer NOT NULL, ",
                            "PLAYER_UUID varchar(36) NOT NULL, ",
                            "FOREIGN KEY(DATABASE_VERSION) REFERENCES GUARDIAN(DATABASE_VERSION), ",
                            "FOREIGN KEY(PUNISHMENT_ID) REFERENCES GUARDIAN_PUNISHMENT(ID)"))
                    .append(")")
                    .build();

            this.databaseVersionTable.execute();
            this.databasePunishmentTable.execute();
            this.databaseLocationTable.execute();
            this.databasePlayerTable.execute();

        } catch (SQLException e) {
            this.plugin.getLogger().error("A problem occurred attempting to create Guardians global database!", e);
        }
    }

    @Override
    public void load() {

    }

    @Override
    public void update() {

    }

    @Override
    public Optional<Connection> getLocation() {
        return Optional.ofNullable(this.connection);
    }
}
