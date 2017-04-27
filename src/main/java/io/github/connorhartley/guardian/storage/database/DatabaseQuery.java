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

import java.sql.Connection;

public class DatabaseQuery {

    private final Connection connection;
    private final String query;

    private DatabaseQuery(Builder builder) {
        this.connection = builder.connection;
        this.query = builder.query;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public String getQuery() {
        return this.query;
    }

    public static class Builder {

        private final Connection connection;
        private String query;

        public Builder(Connection connection) {
            this.connection = connection;
        }

        public Builder append(String query) {
            this.query = new StringBuilder()
                    .append(this.query)
                    .append(" ")
                    .append(query)
                    .toString();
            return this;
        }

        public Builder append(DatabaseQuery query) {
            this.query = new StringBuilder()
                    .append(this.query)
                    .append(" ")
                    .append(query.getQuery())
                    .toString();
            return this;
        }

        public DatabaseQuery build() {
            return new DatabaseQuery(this);
        }

    }

}
