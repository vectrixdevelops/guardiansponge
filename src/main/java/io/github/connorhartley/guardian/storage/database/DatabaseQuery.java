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
package io.github.connorhartley.guardian.storage.database;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseQuery {

    private final Connection connection;
    private final String query;
    private final PreparedStatement preparedStatement;

    private boolean virtual = true;

    private DatabaseQuery(Builder builder) {
        this.connection = builder.connection;
        this.query = builder.query;
        this.preparedStatement = builder.preparedStatement;
    }

    public static Builder builder(Connection connection) {
        return new Builder(connection);
    }

    public Connection getConnection() {
        return this.connection;
    }

    public String getRawQuery() {
        return this.query;
    }

    public PreparedStatement getQuery() {
        return this.preparedStatement;
    }

    public boolean execute() throws SQLException {
        this.virtual = false;

        return this.preparedStatement.execute();
    }

    public ResultSet executeQuery() throws SQLException {
        this.virtual = false;

        return this.preparedStatement.executeQuery();
    }

    public void reset() {
        this.virtual = true;
    }

    public boolean isVirtual() {
        return this.virtual;
    }

    public static class Builder {

        private String query;
        private PreparedStatement preparedStatement;

        private final Connection connection;

        public Builder(Connection connection) {
            this.connection = connection;
        }

        public Builder append(String query) {
            this.query = StringUtils.join(
                    this.query,
                    " ",
                    query
            );
            return this;
        }

        public Builder append(DatabaseQuery query) {
            this.query = StringUtils.join(
                    this.query,
                    " ",
                    query.getRawQuery()
            );
            return this;
        }

        public DatabaseQuery build() throws SQLException {
            this.preparedStatement = this.connection.prepareStatement(this.query);
            return new DatabaseQuery(this);
        }

    }

}
