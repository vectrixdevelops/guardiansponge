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
package io.github.connorhartley.guardian.storage.container;

import io.github.connorhartley.guardian.util.Transformer;
import tech.ferus.util.sql.api.Database;
import tech.ferus.util.sql.api.HandleResults;
import tech.ferus.util.sql.api.Preparer;
import tech.ferus.util.sql.api.ReturnResults;
import tech.ferus.util.sql.core.BasicSql;

import java.util.Optional;

public class DatabaseValue {

    private StorageKey<Database> storageKey;
    private String storageQuery;

    public DatabaseValue(StorageKey<Database> key, String query) {
        storageKey = key;
        storageQuery = query;
    }

    public void execute() {
        execute(s -> {});
    }

    public void execute(Preparer preparer) {
        BasicSql.execute(storageKey.get(), storageQuery, preparer);
    }

    public void query(HandleResults handleResults) {
        query(s -> {}, handleResults);
    }

    public void query(Preparer preparer, HandleResults handleResults) {
        BasicSql.query(storageKey.get(), storageQuery, preparer, handleResults);
    }

    public <T> Optional<T> returnQuery(ReturnResults<T> returnResults) {
        return returnQuery(s -> {}, returnResults);
    }

    public <T> Optional<T> returnQuery(Preparer preparer, ReturnResults<T> returnResults) {
        return BasicSql.returnQuery(storageKey.get(), storageQuery, preparer, returnResults);
    }

    public StorageKey<Database> getKey() {
        return this.storageKey;
    }

    public void setKey(Database key) {
        this.storageKey.set(key);
    }

    public String getQuery() {
        return this.storageQuery;
    }

    public void setQuery(String query) {
        this.storageQuery = query;
    }

    public void transformQuery(Transformer<String> transformer) {
        this.storageQuery = transformer.transform(this.getQuery());
    }

}
