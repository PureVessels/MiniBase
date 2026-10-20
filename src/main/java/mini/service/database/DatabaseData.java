package mini.service.database;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public record DatabaseData(String ip, int port, String database, String username, String password, Map<String, String> properties) {

    public DatabaseData(String ip, int port, String database, String username, String password){
        this(ip, port, database, username, password, Collections.emptyMap());
    }

    public DatabaseData{
        Map<String, String> props = new HashMap<>(properties);

        props.put("cachePrepStmts", "true");
        props.put("prepStmtCacheSqlLimit", "2048");
        props.put("prepStmtCacheSize", "250");
        props.put("useServerPrepStmts", "true");

        properties = Collections.unmodifiableMap(props);
    }
}
