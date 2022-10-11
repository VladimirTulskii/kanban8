package httpclient;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import filemanager.FileBackedTasksManager;
import kvserver.KVServer;
import manager.Managers;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class HTTPTaskManager extends FileBackedTasksManager {
    protected KVTaskClient kvTaskClient;
    protected String path;
    protected Gson gson = new Gson();

    public HTTPTaskManager(String path) {
        this.path = path;
    }

    public void getToken() {
        kvTaskClient = new KVTaskClient(path);
        kvTaskClient.register();
    }

    public void saveTasks() throws IOException {
        if (kvTaskClient == null) {
            System.out.println("Требуется регистрация");
            return;
        }
        this.loadFromFile();
        kvTaskClient.put("/tasks", gson.toJson(getTasks().values()));
        kvTaskClient.put("/epics", gson.toJson(getEpics().values()));
        kvTaskClient.put("/subtasks", gson.toJson(getSubtasks().values()));
        kvTaskClient.put("/history", gson.toJson(historyToString()));
    }

    public void loadTasks() {
        String json = kvTaskClient.load("/tasks");
        Type type = new TypeToken<ArrayList<Task>>(){}.getType();
        ArrayList<Task> tasksList = gson.fromJson(json, type);
        for (Task task : tasksList) {
            addTaskFromKVServer(task);
        }
        allTasks.putAll(getTasks());

        json = kvTaskClient.load("/epics");
        type = new TypeToken<ArrayList<Epic>>(){}.getType();
        ArrayList<Epic> epicsList = gson.fromJson(json, type);
        for (Epic epic : epicsList) {
            addEpicFromKVServer(epic);
        }
        allTasks.putAll(getEpics());

        json = kvTaskClient.load("/subtasks");
        type = new TypeToken<ArrayList<Subtask>>(){}.getType();
        ArrayList<Subtask> subtasksList = gson.fromJson(json, type);
        for (Subtask subtask : subtasksList) {
            addSubtaskFromKVServer(subtask);
        }
        allTasks.putAll(getSubtasks());

        json = kvTaskClient.load("/history");
        String historyLine = json.substring(1, json.length() - 1);
        if (!historyLine.equals("\"\"")) {
            String[] historyLineContents = historyLine.split(",");
            for (String s : historyLineContents) {
                historyManager.add(allTasks.get(Integer.parseInt(s)));
            }
        }
        save();
    }

    public int addTaskFromKVServer(Task task) {
        task.setId(task.getId());
        prioritizedTasks.add(task);
        tasks.put(task.getId(), task);
        save();
        return task.getId();
    }

    public int addEpicFromKVServer(Epic epic) {
        epic.setId(epic.getId());
        prioritizedTasks.add(epic);
        epics.put(epic.getId(), epic);
        save();
        return epic.getId();
    }

    public int addSubtaskFromKVServer(Subtask subtask) {
        subtask.setId(subtask.getId());
        prioritizedTasks.add(subtask);
        subtasks.put(subtask.getId(), subtask);
        save();
        return subtask.getId();
    }

    @Override
    public int add(Task task) {
        task.setId(task.getId());
        getTaskEndTime(task);
        prioritizedTasks.add(task);
        tasks.put(task.getId(), task);
        save();
        return task.getId();
    }
    @Override
    public int add(Epic epic) {
        epic.setId(epic.getId());
        getEpicTimesAndDuration(epic);
        prioritizedTasks.add(epic);
        epics.put(epic.getId(), epic);
        save();
        return epic.getId();
    }
    @Override
    public int add(Subtask subtask) {
        subtask.setId(subtask.getId());
        getSubtaskEndTime(subtask);
        prioritizedTasks.add(subtask);
        subtasks.put(subtask.getId(), subtask);
        save();
        return subtask.getId();
    }

//    public static void main(String[] args) throws IOException {
//        HTTPTaskManager.setIdCounter(1);
//        KVServer kvServer = new KVServer();
//        kvServer.start();
//        HTTPTaskManager manager = Managers.loadedHTTPTasksManager();
//        HTTPTaskManager.setIdCounter(1);
//        manager.getToken();
//        manager.saveTasks();
//        HTTPTaskManager manager1 = new HTTPTaskManager("http://localhost:8078");
//        HTTPTaskManager.setIdCounter(1);
//        manager1.getToken();
//        manager1.loadTasks();
//        System.out.println(manager1.getTasks());
//        System.out.println(manager1.getEpics());
//        System.out.println(manager1.getSubtasks());
//
//        kvServer.stop();
//    }
}
