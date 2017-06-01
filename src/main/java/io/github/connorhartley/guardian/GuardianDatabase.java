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
import io.github.connorhartley.guardian.storage.container.DatabaseValue;
import io.github.connorhartley.guardian.storage.container.StorageKey;
import org.apache.commons.lang3.StringUtils;
import tech.ferus.util.sql.databases.Database;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public final class GuardianDatabase implements StorageProvider<Database> {

    private static final Integer databaseVersion = 1;
    private static final String[] databaseTableNames = { "GUARDIAN_PUNISHMENT", "GUARDIAN_LOCATION", "GUARDIAN_PLAYER" };

    private final Guardian plugin;
    private final Database database;

    public DatabaseValue databaseVersionTable;
    public DatabaseValue databasePunishmentTable;
    public DatabaseValue databaseLocationTable;
    public DatabaseValue databasePlayerTable;

    public GuardianDatabase(Guardian plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
    }

    @Override
    public void create() {
        this.databaseVersionTable = new DatabaseValue(new StorageKey<>(this.database),
                StringUtils.join("CREATE TABLE IF NOT EXISTS GUARDIAN (",
                        "ID integer AUTO_INCREMENT, ",
                        "DATABASE_VERSION integer NOT NULL, ",
                        "SYNCHRONIZE_TIME timestamp NOT NULL, ",
                        "PUNISHMENT_TABLE varchar(24) NOT NULL, ",
                        "LOCATION_TABLE varchar(24) NOT NULL, ",
                        "PLAYER_TABLE varchar(24) NOT NULL, ",
                        "PRIMARY KEY(ID) )"
                ));

        this.databasePunishmentTable = new DatabaseValue(new StorageKey<>(this.database),
                StringUtils.join("CREATE TABLE IF NOT EXISTS GUARDIAN_PUNISHMENT (",
                        "ID integer NOT NULL, ",
                        "DATABASE_VERSION integer NOT NULL, ",
                        "PLAYER_UUID varchar(36) NOT NULL, ",
                        "PUNISHMENT_TYPE varchar(64) NOT NULL, ",
                        "PUNISHMENT_REASON varchar(1024) NOT NULL, ",
                        "PUNISHMENT_TIME timestamp NOT NULL, ",
                        "PUNISHMENT_PROBABILITY double precision NOT NULL, ",
                        "FOREIGN KEY(DATABASE_VERSION) REFERENCES GUARDIAN(DATABASE_VERSION), ",
                        "PRIMARY KEY(ID) )"
                ));

        this.databaseLocationTable = new DatabaseValue(new StorageKey<>(this.database),
                StringUtils.join("CREATE TABLE IF NOT EXISTS GUARDIAN_LOCATION (",
                        "PUNISHMENT_ID integer NOT NULL, ",
                        "DATABASE_VERSION integer NOT NULL, ",
                        "PUNISHMENT_ORDINAL integer NOT NULL, ",
                        "WORLD_UUID varchar(36) NOT NULL, ",
                        "X double precision NOT NULL, ",
                        "Y double precision NOT NULL, ",
                        "Z double precision NOT NULL, ",
                        "FOREIGN KEY(DATABASE_VERSION) REFERENCES GUARDIAN(DATABASE_VERSION), ",
                        "PRIMARY KEY(PUNISHMENT_ID, PUNISHMENT_ORDINAL) )"
                ));

        this.databasePlayerTable = new DatabaseValue(new StorageKey<>(this.database),
                StringUtils.join("CREATE TABLE IF NOT EXISTS GUARDIAN_PLAYER (",
                        "PUNISHMENT_ID integer NOT NULL, ",
                        "DATABASE_VERSION integer NOT NULL, ",
                        "PLAYER_UUID varchar(36) NOT NULL, ",
                        "FOREIGN KEY(DATABASE_VERSION) REFERENCES GUARDIAN(DATABASE_VERSION), ",
                        "FOREIGN KEY(PUNISHMENT_ID) REFERENCES GUARDIAN_PUNISHMENT(ID) )"
                ));

        this.databaseVersionTable.execute();
        this.databasePunishmentTable.execute();
        this.databaseLocationTable.execute();
        this.databasePlayerTable.execute();

        // Temporary as there is no migration system.

        int currentId = new DatabaseValue(new StorageKey<>(this.database), StringUtils.join(
                "SELECT * FROM GUARDIAN WHERE GUARDIAN.DATABASE_VERSION = ?"
        )).returnQuery(
                s -> s.setInt(1, databaseVersion),
                r -> r.getInt("ID")
        ).orElseGet(() -> {
            new DatabaseValue(new StorageKey<>(this.database), StringUtils.join(
                    "INSERT INTO GUARDIAN (",
                    "DATABASE_VERSION, ",
                    "SYNCHRONIZE_TIME, ",
                    "PUNISHMENT_TABLE, ",
                    "LOCATION_TABLE, ",
                    "PLAYER_TABLE) ",
                    "VALUES (?, ?, ?, ?, ?)"
            )).execute(
                    s -> {
                        s.setInt(1, databaseVersion);
                        s.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                        s.setString(3, databaseTableNames[0]);
                        s.setString(4, databaseTableNames[1]);
                        s.setString(5, databaseTableNames[2]);
                    }
            );

            return databaseVersion;
        });
    }

    @Override
    public void load() {

    }

    @Override
    public void update() {

    }

    @Override
    public Optional<Database> getLocation() {
        return Optional.ofNullable(this.database);
    }
}
