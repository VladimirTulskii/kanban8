package httpserver;

import httpclient.HTTPTaskManager;
import kvserver.KVServer;
import manager.Managers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class LoadFromKVServerTest {
    HTTPTaskManager httpTaskManager;

    @BeforeEach
    void setUp() {

    }

    @Test
    void saveAndLoadTasksKVServerTest() throws IOException {
        KVServer kvServer = new KVServer();
        kvServer.start();
        httpTaskManager = Managers.loadedHTTPTasksManager();
        httpTaskManager.getToken();
        httpTaskManager.saveTasks();
        HTTPTaskManager loadedFromServerManager = new HTTPTaskManager("http://localhost:8078");
        loadedFromServerManager.getToken();
        loadedFromServerManager.loadTasks();

        kvServer.stop();
    }

}
