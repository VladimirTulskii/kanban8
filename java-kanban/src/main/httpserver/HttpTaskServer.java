package httpserver;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import httpclient.HttpTaskManager;
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
    HttpServer httpServer;
    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static Gson gson = new Gson();
    protected static HttpTaskManager httpTaskManager = Managers.getDefaultHttpTaskManager("http://localhost:8078", false);

    public HttpTaskServer() throws IOException {
        this.httpServer = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        httpServer.createContext("/tasks", new TasksHandler());
    }

    public static HttpTaskManager getHttpTaskManager() {
        return httpTaskManager;
    }

    public void start() {

        System.out.println("Запускаем сервер на порту " + PORT);

        httpServer.start();
    }

    public void stop() {
        System.out.println("Сервер остановлен");
        httpServer.stop(0);
    }

    static class TasksHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            String response = null;
            int rCode;
            switch (method) {
                case "GET":
                    response = GETRequest(path, exchange);
                    rCode = 200;
                    break;
                case "POST":
                    POSTRequest(path, exchange);
                    rCode = 201;
                    break;
                case "DELETE":
                    response = DELETERequest(path, exchange);
                    rCode = 200;
                    break;
                default:
                    response = gson.toJson("bad method type. use GET, POST or DELETE please");
                    rCode = 400;
            }

            exchange.sendResponseHeaders(rCode, 0);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private static String GETRequest(String path, HttpExchange exchange) {
        String response = null;
        if (Pattern.matches("^/tasks$", path)) {
            response = gson.toJson(httpTaskManager.getAllTasks().values());
        } else if (Pattern.matches("^/tasks/task$", path) && exchange.getRequestURI().getQuery() != null) {
            String param = exchange.getRequestURI().getQuery();
            String[] paramArray = param.split("=");
            int id = Integer.parseInt(paramArray[1]);
            response = gson.toJson(httpTaskManager.getTaskById(id));
        } else if (Pattern.matches("^/tasks/task$", path)) {
            response = gson.toJson(httpTaskManager.getTasks().values());
        } else if (Pattern.matches("^/tasks/subtask/epic$", path) && exchange.getRequestURI().getQuery() != null) {
            String param = exchange.getRequestURI().getQuery();
            String[] paramArray = param.split("=");
            int id = Integer.parseInt(paramArray[1]);
            response = gson.toJson(httpTaskManager.getAllSubtasksByEpic(id));
        } else if (Pattern.matches("^/tasks/epic$", path) && exchange.getRequestURI().getQuery() != null) {
            String param = exchange.getRequestURI().getQuery();
            String[] paramArray = param.split("=");
            int id = Integer.parseInt(paramArray[1]);
            response = gson.toJson(httpTaskManager.getEpicById(id));
        } else if (Pattern.matches("^/tasks/epic$", path)) {
            response = gson.toJson(httpTaskManager.getEpics().values());
        } else if (Pattern.matches("^/tasks/subtask$", path) && exchange.getRequestURI().getQuery() != null) {
            String param = exchange.getRequestURI().getQuery();
            String[] paramArray = param.split("=");
            int id = Integer.parseInt(paramArray[1]);
            response = gson.toJson(httpTaskManager.getSubtaskById(id));
        } else if (Pattern.matches("^/tasks/subtask$", path)) {
            response = gson.toJson(httpTaskManager.getSubtasks().values());
        } else if (Pattern.matches("^/tasks/history$", path)) {
            response = gson.toJson(httpTaskManager.getHistory());
        } else if (Pattern.matches("^/tasks/priority$", path)) {
            response = gson.toJson(httpTaskManager.getPrioritizedTasks());
        }
        return response;
    }

    private static void POSTRequest(String path, HttpExchange exchange) throws IOException {
        if (Pattern.matches("^/tasks/task$", path)) {
            InputStream inputStream = exchange.getRequestBody();
            String taskBody = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            Task task = gson.fromJson(taskBody, Task.class);
            //Task task = gson.fromJson(new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET), Task.class);
            if (httpTaskManager.getTasks().containsKey(task.getId())) {
                httpTaskManager.update(task);
                httpTaskManager.getAllTasks().put(task.getId(), task);
            } else {
                httpTaskManager.add(task);
                httpTaskManager.getAllTasks().put(task.getId(), task);
            }
        } else if (Pattern.matches("^/tasks/epic$", path)) {
            InputStream inputStream = exchange.getRequestBody();
            String epicBody = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            Epic epic = gson.fromJson(epicBody, Epic.class);
            if (httpTaskManager.getTasks().containsKey(epic.getId())) {
                httpTaskManager.update(epic);
                httpTaskManager.getAllTasks().put(epic.getId(), epic);
            } else {
                httpTaskManager.add(epic);
                httpTaskManager.getAllTasks().put(epic.getId(), epic);
            }
        } else if (Pattern.matches("^/tasks/subtask$", path)) {
            InputStream inputStream = exchange.getRequestBody();
            String subtaskBody = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            Subtask subtask = gson.fromJson(subtaskBody, Subtask.class);
            if (httpTaskManager.getTasks().containsKey(subtask.getId())) {
                httpTaskManager.update(subtask);
                httpTaskManager.getAllTasks().put(subtask.getId(), subtask);
            } else {
                httpTaskManager.add(subtask);
                httpTaskManager.getAllTasks().put(subtask.getId(), subtask);
            }
        }
    }

    private static String DELETERequest(String path, HttpExchange exchange) {
        String response = null;
        if (Pattern.matches("^/tasks/task$", path) && exchange.getRequestURI().getQuery() != null) {
            String param = exchange.getRequestURI().getQuery();
            String[] paramArray = param.split("=");
            int id = Integer.parseInt(paramArray[1]);
            httpTaskManager.removeTaskById(id);
            response = gson.toJson("task id=" + id + " deleted");
        } else if (Pattern.matches("^/tasks/task$", path)) {
            httpTaskManager.removeAllTasks();
            response = gson.toJson("tasks deleted");
        } else if (Pattern.matches("^/tasks/epic$", path) && exchange.getRequestURI().getQuery() != null) {
            String param = exchange.getRequestURI().getQuery();
            String[] paramArray = param.split("=");
            int id = Integer.parseInt(paramArray[1]);
            httpTaskManager.removeEpicById(id);
            response = gson.toJson("epic id=" + id + " deleted");
        } else if (Pattern.matches("^/tasks/epic$", path)) {
            httpTaskManager.removeAllEpicsAndSubtasks();
            response = gson.toJson("epics and subtasks deleted");
        } else if (Pattern.matches("^/tasks/subtask$", path) && exchange.getRequestURI().getQuery() != null) {
            String param = exchange.getRequestURI().getQuery();
            String[] paramArray = param.split("=");
            int id = Integer.parseInt(paramArray[1]);
            httpTaskManager.removeSubtaskById(id);
            response = gson.toJson("subtask id=" + id + " deleted");
            System.out.println(httpTaskManager.getPrioritizedTasks());
        } else {
            response = gson.toJson("wrong operation");
        }
        return response;
    }
}