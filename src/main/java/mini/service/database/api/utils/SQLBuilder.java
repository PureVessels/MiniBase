package mini.service.database.api.utils;

import mini.service.database.api.thrown.DatabaseException;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class SQLBuilder {

    private final String tableName;
    private final LinkedHashSet<String> columns;

    private final Set<String> tableColumns;

    public SQLBuilder(@NotNull String tableName, @NotNull LinkedHashSet<String> columns) {
        this.tableName = tableName;
        if (tableName.isBlank()) throw new IllegalArgumentException("Table name cannot be blank.");

        this.columns = new LinkedHashSet<>(columns);
        if (columns.isEmpty()) throw new IllegalArgumentException("Columns cannot be empty.");

        this.tableColumns = Collections.unmodifiableSet(columns);
    }

    public String getTableName(){ return tableName; }
    public Set<String> getTableColumns() { return tableColumns; }
    public int getTableColumnsSize(){ return columns.size(); }


    public String createFindSQL(@NotNull List<String> columns){ return createFindSQL(columns,1); }
    public String createFindSQL(@NotNull List<String> columns, int limit){ return createFindSQL(columns, "*", limit); }
    public String createFindSQL(@NotNull List<String> whereColumns, @NotNull String selectValue, int limit){
        for (String column : whereColumns){ allowedColumns(column); }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(selectValue).append(" FROM ").append(tableName);
        buildWhereSQL(sql, whereColumns);
        if (limit > 0) sql.append(" LIMIT ").append(limit);

        return sql.toString();
    }

    public String createSelectSQL(@NotNull List<String> keys, @NotNull List<String> whereColumns){ return createSelectSQL(keys, whereColumns, 1); }
    public String createSelectSQL(@NotNull List<String> keys, @NotNull List<String> whereColumns, int limit){
        for (String column : keys){  allowedColumns(column); }
        for (String column : whereColumns){ allowedColumns(column); }

        StringBuilder sql = new StringBuilder();
        if (keys.isEmpty()) {
            throw new IllegalArgumentException("SELECT condition required");
        }

        sql.append("SELECT ");
        builderColumns(sql, keys, ", ", null);

        sql.append(" FROM ").append(getTableName());

        if (whereColumns.isEmpty()) {
            throw new IllegalArgumentException("WHERE condition required");
        }

        buildWhereSQL(sql, whereColumns);

        if (limit > 0) sql.append(" LIMIT ").append(limit);
        return sql.toString();
    }

    public String createUpdateSQL(@NotNull List<String> updates, @NotNull List<String> whereColumns){ return createUpdateSQL(updates, whereColumns, 1); }
    public String createUpdateSQL(@NotNull List<String> updates, @NotNull List<String> whereColumns, int limit){
        if (updates.isEmpty()) throw new IllegalArgumentException("UPDATE condition required");

        for (String column : updates){ allowedColumns(column); }
        for (String column : whereColumns){ allowedColumns(column); }

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ");
        sql.append(getTableName()).append(" ");

        sql.append("SET ");
        builderColumns(sql, updates, ", ", " = ?");

        buildWhereSQL(sql, whereColumns);

        if (limit > 0) sql.append(" LIMIT ").append(limit);
        return sql.toString();
    }

    public String createUpdateAtomicIntegerSQL(@NotNull List<String> updates, @NotNull List<String> whereColumns){ return createUpdateAtomicIntegerSQL(updates, whereColumns, 1); }
    public String createUpdateAtomicIntegerSQL(@NotNull List<String> updates, @NotNull List<String> whereColumns, int limit){
        return createUpdateAtomicSQL(updates, whereColumns, AtomicType.NUMBER, limit);
    }

    public String createUpdateAtomicStringSQL(@NotNull List<String> updates, @NotNull List<String> whereColumns){ return createUpdateAtomicStringSQL(updates, whereColumns, 1); }
    public String createUpdateAtomicStringSQL(@NotNull List<String> updates, @NotNull List<String> whereColumns, int limit){ return createUpdateAtomicSQL(updates, whereColumns, AtomicType.STRING, limit); }

    public String createUpdateAtomicSQL(@NotNull List<String> updates, @NotNull List<String> whereColumns, @NotNull AtomicType type){ return createUpdateAtomicSQL(updates, whereColumns, type, 1); }
    public String createUpdateAtomicSQL(@NotNull List<String> updates, @NotNull List<String> whereColumns, @NotNull AtomicType type, int limit){
        if (updates.isEmpty()) throw new IllegalArgumentException("UPDATE condition required");

        for (String column : updates){ allowedColumns(column); }
        for (String column : whereColumns){ allowedColumns(column); }

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ");
        sql.append(getTableName()).append(" ");
        sql.append("SET ");

        String suffix = switch (type) {
            case NUMBER -> " = ${key} + ?";
            case STRING -> " = CONCAT(COALESCE(${key}, ''), ?)";
        };

        builderColumns(sql, updates, ", ", suffix);

        buildWhereSQL(sql, whereColumns);

        if (limit > 0) sql.append(" LIMIT ").append(limit);
        return sql.toString();

    }

    public String createInsertSQL() { return createInsertSQL(false); }
    public String createInsertIgnoreSQL() { return createInsertSQL(true); }

    public String createUpsertSQL(@NotNull List<String> updateColumns) {
        if (updateColumns.isEmpty()) { throw new IllegalArgumentException("UPSERT update columns required"); }

        StringBuilder sql = new StringBuilder(createInsertSQL(false));
        sql.append(" ON DUPLICATE KEY UPDATE ");
        builderColumns(sql, updateColumns, ", ", " = ?");

        return sql.toString();
    }

    public String createInsertSQL(boolean ignored){
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT");
        if (ignored) sql.append(" IGNORE");
        sql.append(" INTO ").append(getTableName());
        sql.append(" (");

        int i = 0;
        for (String column : getTableColumns()) {
            sql.append(column);
            if (i < getTableColumnsSize() - 1) sql.append(", ");
            i++;
        }

        sql.append(") VALUES (");
        for (i = 0; i < getTableColumnsSize(); i++) {
            sql.append("?");
            if (i < getTableColumnsSize() - 1) sql.append(", ");
        }
        sql.append(")");
        return sql.toString();
    }

    private void buildWhereSQL(StringBuilder sql, List<String> whereColumns){
        if (whereColumns.isEmpty()) throw new IllegalArgumentException("WHERE condition required");
        sql.append(" WHERE ");
        builderColumns(sql, whereColumns, " AND ", " = ?");
    }

    private void builderColumns(StringBuilder sql, List<String> columns, String separator, String suffix) {
        if (columns.isEmpty()) { throw new IllegalArgumentException("Columns required"); }

        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            allowedColumns(column);

            if (i > 0) { sql.append(key(separator, column)); }
            sql.append(column);
            if (suffix != null) { sql.append(key(suffix, column)); }
        }
    }

    private String key(String key, String column){ return key.replace("${key}", column); }
    private void allowedColumns(String column){ if(!getTableColumns().contains(column)) throw new DatabaseException("SQl create failed because " + column + " not column", DatabaseException.Function.BUILDER); }


    public enum AtomicType {
        NUMBER,
        STRING
    }
}

