package mini.service.database.api.utils;

import org.jetbrains.annotations.NotNull;

import mini.librariy.ValueType;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public sealed interface SQLValueType<T> permits SQLValueType.ValueSQLType {

    @NotNull Class<T> getValueType();

    void set(PreparedStatement ps, int index, T value) throws SQLException;
    @Nullable T get(ResultSet rs, String column) throws SQLException;

    SQLValueType<String> STRING = new ValueSQLType<>(String.class);

    SQLValueType<Boolean> BOOLEAN = new ValueSQLType<>(Boolean.class);

    SQLValueType<Integer> INTEGER = new ValueSQLType<>(Integer.class);
    SQLValueType<Double> DOUBLE = new ValueSQLType<>(Double.class);
    SQLValueType<Long> LONG = new ValueSQLType<>(Long.class);

    final class ValueSQLType<T> implements SQLValueType<T> {
        private final Class<T> clazz;

        ValueSQLType(Class<T> clazz) { this.clazz = clazz; }

        @NotNull public Class<T> getValueType() { return clazz; }

        public void set(PreparedStatement ps, int index, T value) throws SQLException {
            if (value == null) ps.setNull(index, Types.NULL);
            else ps.setObject(index, value);
        }

        public @Nullable T get(ResultSet rs, String column) throws SQLException {
            Object obj = rs.getObject(column);
            if (obj == null) return null;
            return clazz.cast(obj);
        }
    }
}
