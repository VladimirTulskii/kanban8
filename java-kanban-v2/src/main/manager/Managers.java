package manager;

import filemanager.FileBackedTasksManager;
import httpclient.HTTPTaskManager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static FileBackedTasksManager getDefaultFileManager() {
        return new FileBackedTasksManager();
    }

    public static HTTPTaskManager loadedHTTPTasksManager() {
        HTTPTaskManager httpTaskManager = new HTTPTaskManager("http://localhost:8078");
        httpTaskManager.loadFromFile();
        return httpTaskManager;
    }

//    public static FileBackedTasksManager loadedFromFileTasksManager() {
//        var fileManager = new FileBackedTasksManager();
//        fileManager.loadFromFile();
//        return fileManager;
//    }
}
