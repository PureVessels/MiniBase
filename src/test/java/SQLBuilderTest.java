import mini.service.database.api.utils.SQLBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SQLBuilderTest {

    private SQLBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new SQLBuilder(
                "test", new LinkedHashSet<>(List.of("column1", "column2", "column3"))
        );
    }

    @Test
    void shouldCreateFindSQL() {
        String sql = builder.createFindSQL(List.of("column1"));
        String sql2 = builder.createFindSQL(List.of("column1"), 2);
        String sql3 = builder.createFindSQL(List.of("column1"), 0);

        assertEquals("SELECT * FROM test WHERE column1 = ? LIMIT 1", sql);
        assertEquals("SELECT * FROM test WHERE column1 = ? LIMIT 2", sql2);
        assertEquals("SELECT * FROM test WHERE column1 = ?", sql3);
    }

    @Test
    void shouldCreateUpdateSQL() {
        String sql = builder.createUpdateSQL(List.of("column2", "column3"), List.of("column1"));
        String sql2 = builder.createUpdateSQL(List.of("column2", "column3"), List.of("column1"), 2);
        String sql3 = builder.createUpdateSQL(List.of("column2", "column3"), List.of("column1"), 0);

        assertEquals("UPDATE test SET column2 = ?, column3 = ? WHERE column1 = ? LIMIT 1", sql);
        assertEquals("UPDATE test SET column2 = ?, column3 = ? WHERE column1 = ? LIMIT 2", sql2);
        assertEquals("UPDATE test SET column2 = ?, column3 = ? WHERE column1 = ?", sql3);
    }

    @Test
    void shouldCreateSelectSQL() {
        String sql = builder.createSelectSQL(List.of("column2"), List.of("column1"));

        String sql2 = builder.createSelectSQL(List.of("column2", "column3"), List.of("column1"), 2);

        String sql3 = builder.createSelectSQL(List.of("column2", "column3"), List.of("column1"), 0);

        assertEquals("SELECT column2 FROM test WHERE column1 = ? LIMIT 1", sql);
        assertEquals("SELECT column2, column3 FROM test WHERE column1 = ? LIMIT 2", sql2);
        assertEquals("SELECT column2, column3 FROM test WHERE column1 = ?", sql3);
    }

    @Test
    void shouldCreateInsertSQL() {
        String sql = builder.createInsertSQL();

        assertEquals("INSERT INTO test (column1, column2, column3) VALUES (?, ?, ?)", sql);
    }

    @Test
    void shouldCreateInsertIgnoreSQL() {
        String sql = builder.createInsertIgnoreSQL();

        assertEquals("INSERT IGNORE INTO test (column1, column2, column3) VALUES (?, ?, ?)", sql);
    }

    @Test
    void shouldCreateUpsertSQL() {
        String sql = builder.createUpsertSQL(List.of("column2", "column3"));

        assertEquals(
                "INSERT INTO test (column1, column2, column3) VALUES (?, ?, ?) " + "ON DUPLICATE KEY UPDATE column2 = ?, column3 = ?", sql
        );
    }

    @Test
    void shouldCreateAtomicNumberUpdateSQL() {
        String sql = builder.createUpdateAtomicIntegerSQL(List.of("column2"), List.of("column1"));

        assertEquals("UPDATE test SET column2 = column2 + ? WHERE column1 = ? LIMIT 1", sql);
    }

    @Test
    void shouldCreateAtomicNumberUpdateSQLWithLimit() {
        String sql = builder.createUpdateAtomicIntegerSQL(List.of("column2", "column3"), List.of("column1"), 2);

        assertEquals("UPDATE test SET column2 = column2 + ?, column3 = column3 + ? " + "WHERE column1 = ? LIMIT 2", sql);
    }

    @Test
    void shouldCreateAtomicStringUpdateSQL() {
        String sql = builder.createUpdateAtomicStringSQL(List.of("column2"), List.of("column1"));

        assertEquals("UPDATE test SET column2 = CONCAT(COALESCE(column2, ''), ?) " + "WHERE column1 = ? LIMIT 1", sql);
    }

    @Test
    void shouldCreateDeleteSQL() {
        String sql = builder.createDeleteSQL(List.of("column1", "column2"));

        assertEquals("DELETE FROM test WHERE column1 = ?, column2 = ?", sql);
    }
}