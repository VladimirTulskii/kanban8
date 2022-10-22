package manager;

import com.google.gson.Gson;
import filemanager.FileBackedTasksManager;
import httpclient.HttpTaskManager;

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

    public static HttpTaskManager getDefaultHttpTaskManager(String path, boolean load) {
        return new HttpTaskManager(path, load);
    }

    public static Gson getGson() {
        Gson gson = new Gson();
        return gson;
    }
}
