package httpserver;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import filemanager.FileBackedTasksManager;
import httpclient.HTTPTaskManager;
import manager.Managers;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static Gson gson = new Gson();
    protected static HTTPTaskManager httpTaskManager = Managers.loadedHTTPTasksManager();

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new TasksHandler());
        httpServer.start();

        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    static class TasksHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            String response = null;
            int rCode = 404;
            switch (method) {
                case "GET":
                    if (Pattern.matches("^/tasks$", path)) {
                        response = gson.toJson(httpTaskManager.getAllTasks().values());
                        rCode = 200;
                    } else if (Pattern.matches("^/tasks/task$", path) && exchange.getRequestURI().getQuery() != null) {
                        String param = exchange.getRequestURI().getQuery();
                        String[] paramArray = param.split("=");
                        int id = Integer.parseInt(paramArray[1]);
                        response = gson.toJson(httpTaskManager.getTaskById(id));
                        rCode = 200;
                    } else if (Pattern.matches("^/tasks/task$", path)) {
                        response = gson.toJson(httpTaskManager.getTasks().values());
                        rCode = 200;
                    } else if (Pattern.matches("^/tasks/subtask/epic$", path) && exchange.getRequestURI().getQuery() != null) {
                        String param = exchange.getRequestURI().getQuery();
                        String[] paramArray = param.split("=");
                        int id = Integer.parseInt(paramArray[1]);
                        response = gson.toJson(httpTaskManager.getAllSubtasksByEpic(id));
                        rCode = 200;
                    } else if (Pattern.matches("^/tasks/epic$", path) && exchange.getRequestURI().getQuery() != null) {
                        String param = exchange.getRequestURI().getQuery();
                        String[] paramArray = param.split("=");
                        int id = Integer.parseInt(paramArray[1]);
                        response = gson.toJson(httpTaskManager.getEpicById(id));
                        rCode = 200;
                    } else if (Pattern.matches("^/tasks/epic$", path)) {
                        response = gson.toJson(httpTaskManager.getEpics().values());
                        rCode = 200;
                    } else if (Pattern.matches("^/tasks/subtask$", path) && exchange.getRequestURI().getQuery() != null) {
                        String param = exchange.getRequestURI().getQuery();
                        String[] paramArray = param.split("=");
                        int id = Integer.parseInt(paramArray[1]);
                        response = gson.toJson(httpTaskManager.getSubtaskById(id));
                        rCode = 200;
                    } else if (Pattern.matches("^/tasks/subtask$", path)) {
                        response = gson.toJson(httpTaskManager.getSubtasks().values());
                        rCode = 200;
                    } else if (Pattern.matches("^/tasks/history$", path)) {
                        response = gson.toJson(httpTaskManager.getHistory());
                        rCode = 200;
                    } else if (Pattern.matches("^/tasks/priority$", path)) {
                        response = gson.toJson(httpTaskManager.getPrioritizedTasks());
                        rCode = 200;
                    }
                    break;
                case "POST":
                    if (Pattern.matches("^/tasks/task$", path)) {
                        InputStream inputStream = exchange.getRequestBody();
                        String taskBody = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                        Task task = gson.fromJson(taskBody, Task.class);
                        if (httpTaskManager.getTasks().containsKey(task.getId())) {
                            httpTaskManager.update(task);
                        } else {
                            httpTaskManager.add(task);
                        }
                        rCode = 201;
                    } else if (Pattern.matches("^/tasks/epic$", path)) {
                        InputStream inputStream = exchange.getRequestBody();
                        String epicBody = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                        Epic epic = gson.fromJson(epicBody, Epic.class);
                        if (httpTaskManager.getTasks().containsKey(epic.getId())) {
                            httpTaskManager.update(epic);
                        } else {
                            httpTaskManager.add(epic);
                        }
                        rCode = 201;
                    } else if (Pattern.matches("^/tasks/subtask$", path)) {
                        InputStream inputStream = exchange.getRequestBody();
                        String subtaskBody = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                        Subtask subtask = gson.fromJson(subtaskBody, Subtask.class);
                        if (httpTaskManager.getTasks().containsKey(subtask.getId())) {
                            httpTaskManager.update(subtask);
                        } else {
                            httpTaskManager.add(subtask);
                        }
                        rCode = 201;
                    }
                    break;
                case "DELETE":
                    if (Pattern.matches("^/tasks/task$", path) && exchange.getRequestURI().getQuery() != null) {
                        String param = exchange.getRequestURI().getQuery();
                        String[] paramArray = param.split("=");
                        int id = Integer.parseInt(paramArray[1]);
                        httpTaskManager.removeTaskById(id);
                        response = gson.toJson("task id=" + id + " deleted");
                        rCode = 200;
                    } else if (Pattern.matches("^/tasks/task$", path)) {
                        httpTaskManager.removeAllTasks();
                        rCode = 200;
                        response = gson.toJson("tasks deleted");
                    } else if (Pattern.matches("^/tasks/epic$", path) && exchange.getRequestURI().getQuery() != null) {
                        String param = exchange.getRequestURI().getQuery();
                        String[] paramArray = param.split("=");
                        int id = Integer.parseInt(paramArray[1]);
                        httpTaskManager.removeEpicById(id);
                        rCode = 200;
                        response = gson.toJson("epic id=" + id + " deleted");
                    } else if (Pattern.matches("^/tasks/epic$", path)) {
                        httpTaskManager.removeAllEpicsAndSubtasks();
                        rCode = 200;
                        response = gson.toJson("epics and subtasks deleted");
                    } else if (Pattern.matches("^/tasks/subtask$", path) && exchange.getRequestURI().getQuery() != null) {
                        String param = exchange.getRequestURI().getQuery();
                        String[] paramArray = param.split("=");
                        int id = Integer.parseInt(paramArray[1]);
                        httpTaskManager.removeSubtaskById(id);
                        rCode = 200;
                        response = gson.toJson("subtask id=" + id + " deleted");
                        System.out.println(httpTaskManager.getPrioritizedTasks());
                    } else {
                        rCode = 404;
                        response = gson.toJson("wrong operation");
                    }
                    break;
                default:
                    response = gson.toJson("bad method type. use GET, POST or DELETE please");
                    rCode = 200;
            }

            exchange.sendResponseHeaders(rCode, 0);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}