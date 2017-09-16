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
package io.github.connorhartley.guardian.entry;

import com.google.common.reflect.TypeToken;
import com.ichorpowered.guardian.api.entry.EntityEntry;
import net.kyori.lunar.reflect.Reified;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuardianEntityEntry<T> implements EntityEntry, Reified<T> {

    private final T entity;
    private final UUID uuid;

    public static <E> GuardianEntityEntry<E> of(E entity, UUID uuid) {
        return new GuardianEntityEntry<>(entity, uuid);
    }

    private GuardianEntityEntry(@Nullable T entity, @Nonnull UUID uuid) {
        this.entity = entity;
        this.uuid = uuid;
    }

    @Nonnull
    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Nonnull
    @Override
    public <E> Optional<E> getEntity(TypeToken<E> typeToken) {
        if (!typeToken.getType().equals(this.entity)) return Optional.empty();
        return Optional.ofNullable((E) this.entity);
    }

    @Nonnull
    @Override
    public TypeToken<T> type() {
        return TypeToken.of((Class<T>) this.entity.getClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.entity, this.uuid);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) return true;
        if (object == null || !(object instanceof EntityEntry)) return false;
        return Objects.equals(this.entity, ((EntityEntry) object).getEntity(type()))
                && Objects.equals(this.uuid, ((EntityEntry) object).getUniqueId());
    }

}
