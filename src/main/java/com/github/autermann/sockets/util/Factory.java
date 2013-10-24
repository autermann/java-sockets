/*
 * Copyright (C) 2013-2013 by it's authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.autermann.sockets.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class Factory<T> {

    public abstract T create();

    public static <T> Factory<T> fromInstance(T instance) {
        return new InstanceFactory<T>(checkNotNull(instance));
    }

    private static class InstanceFactory<T> extends Factory<T> {
        private final T instance;

        private InstanceFactory(T instance) {
            this.instance = instance;
        }

        @Override
        public T create() {
            return instance;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.instance);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof InstanceFactory) {
                InstanceFactory<?> other = (InstanceFactory) obj;
                return Objects.equal(this, other.instance);
            }
            return false;
        }

        @Override
        public String toString() {
            return "Factory.fromInstance(" + instance + ")";
        }
    }

}
