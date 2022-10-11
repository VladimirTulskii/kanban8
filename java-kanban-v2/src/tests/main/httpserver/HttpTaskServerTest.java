package httpserver;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import filemanager.FileBackedTasksManager;
import filemanager.TaskType;
import httpclient.HTTPTaskManager;
import kvserver.KVServer;
import manager.Managers;
import org.junit.jupiter.api.*;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HttpTaskServerTest {
    HttpTaskServer httpTaskServer;
    HTTPTaskManager httpTaskManager;
    HTTPTaskManager loadedTaskManager;
    HttpClient httpClient = HttpClient.newHttpClient();

    KVServer kvServer;
    Gson gson = new Gson();
    String path = "http://localhost:8080";

    Task task1 = new Task(HTTPTaskManager.getIdCounter(), TaskType.TASK, "Продать авто",
            "продать", Status.NEW, LocalDateTime.of(2022, 9, 25, 13, 30, 15), 30);
    Task task2 = new Task(HTTPTaskManager.getIdCounter(), TaskType.TASK, "Потратиться на себя",
            "Сделать себе приятно", Status.NEW, LocalDateTime.of(2022, 9, 26, 12, 0, 15), 30);
    Epic epic1 = new Epic(FileBackedTasksManager.getIdCounter(), TaskType.EPIC, "Пройти курс Java-разработчик",
            "пройти все спринты", Status.NEW);
    Subtask subtask1 = new Subtask(FileBackedTasksManager.getIdCounter(), TaskType.SUBTASK, "Пройти Java Core",
            "База", Status.NEW, 7, LocalDateTime.now(), 30);
    Subtask subtask2 = new Subtask(FileBackedTasksManager.getIdCounter(), TaskType.SUBTASK, "Стать гуру Spring",
            "Важная задача", Status.NEW, 7, LocalDateTime.of(2022, 9, 24, 10, 0, 15), 45);
    Subtask subtask3 = new Subtask(FileBackedTasksManager.getIdCounter(), TaskType.SUBTASK, "Сдать тесты",
            "Важная задача", Status.NEW, 7, LocalDateTime.of(2022, 9, 25, 11, 0, 15), 120);
    Epic epic2 = new Epic(FileBackedTasksManager.getIdCounter(), TaskType.EPIC, "Английский",
            "дойти до уровня Native", Status.NEW);

    public HttpTaskServerTest() {
        loadedTaskManager = Managers.loadedHTTPTasksManager();
    }

    @BeforeAll
    static void constructFileForeTests() {
        var fileManager = Managers.getDefaultFileManager();
        FileBackedTasksManager.setIdCounter(1);
        //создаем объекты и закидываем в файл
        fileManager.add(new Task(FileBackedTasksManager.getIdCounter(), TaskType.TASK, "Продать авто",
                "продать", Status.NEW, LocalDateTime.of(2022, 9, 25, 13, 30, 15), 30));
        fileManager.add(new Task(FileBackedTasksManager.getIdCounter(), TaskType.TASK, "Потратиться на себя",
                "Сделать себе приятно", Status.NEW, LocalDateTime.of(2022, 9, 26, 12, 0, 15), 30));
        fileManager.add(new Epic(FileBackedTasksManager.getIdCounter(), TaskType.EPIC, "Пройти курс Java-разработчик",
                "пройти все спринты", Status.NEW));
        fileManager.add(new Subtask(FileBackedTasksManager.getIdCounter(), TaskType.SUBTASK, "Пройти Java Core",
                "База", Status.NEW, 7, LocalDateTime.now(), 30));
        fileManager.add(new Subtask(FileBackedTasksManager.getIdCounter(), TaskType.SUBTASK, "Стать гуру Spring",
                "Важная задача", Status.NEW, 7, LocalDateTime.of(2022, 9, 24, 10, 0, 15), 45));
        fileManager.add(new Subtask(FileBackedTasksManager.getIdCounter(), TaskType.SUBTASK, "Сдать тесты",
                "Важная задача", Status.NEW, 7, LocalDateTime.of(2022, 9, 25, 11, 0, 15), 120));
        fileManager.add(new Epic(FileBackedTasksManager.getIdCounter(), TaskType.EPIC, "Английский",
                "дойти до уровня Native", Status.NEW));

        fileManager.getTaskById(1);
        fileManager.getEpicById(7);
        fileManager.getEpicById(3);
    }


    @BeforeEach
    void starServer() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
        httpTaskServer = new HttpTaskServer();
        httpTaskServer.start();
        httpTaskManager = Managers.loadedHTTPTasksManager();
        httpTaskManager.getToken();
        httpTaskManager.saveTasks();
    }

    @AfterEach
    void stopStart() {
        httpTaskServer.stop();
        kvServer.stop();
    }

    @Test
    void getAllTasksAndEpicsAndSubtasks() throws IOException {
        httpTaskManager = loadedTaskManager;
        httpTaskManager.saveTasks();
        URI url = URI.create(path + "/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            String json = response.body();
            Type type = new TypeToken<ArrayList<Task>>(){}.getType();
            List<Task> tasksList = gson.fromJson(json, type);
            List<Task> expectedList = new ArrayList<>(httpTaskManager.getAllTasks().values());
                assertEquals(expectedList.size(), tasksList.size());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getTasksTest() {
        URI url = URI.create(path + "/tasks/task");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            String json = response.body();
            Type type = new TypeToken<ArrayList<Task>>(){}.getType();
            List<Task> tasksList = gson.fromJson(json, type);
            List<Task> expectedList = new ArrayList<>(httpTaskManager.getTasks().values());
            for (int i = 0; i < tasksList.size(); i++) {
                assertEquals(expectedList.get(i).getTitle(), tasksList.get(i).getTitle());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getEpicsTest() {
        URI url = URI.create(path + "/tasks/epic");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            String json = response.body();
            Type type = new TypeToken<ArrayList<Epic>>(){}.getType();
            List<Epic> tasksList = gson.fromJson(json, type);
            List<Epic> expectedList = new ArrayList<>(httpTaskManager.getEpics().values());
            for (int i = 0; i < tasksList.size(); i++) {
                assertEquals(expectedList.get(i).getTitle(), tasksList.get(i).getTitle());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getSubtasksTest() {
        URI url = URI.create(path + "/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            String json = response.body();
            Type type = new TypeToken<ArrayList<Subtask>>(){}.getType();
            List<Subtask> tasksList = gson.fromJson(json, type);
            List<Subtask> expectedList = new ArrayList<>(httpTaskManager.getSubtasks().values());
            for (int i = 0; i < tasksList.size(); i++) {
                assertEquals(expectedList.get(i).getTitle(), tasksList.get(i).getTitle());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getTaskByIdTest() {
        URI url = URI.create(path + "/tasks/task?id=1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            String json = response.body();
            System.out.println(response.body());
            Task task = gson.fromJson(json, Task.class);
            Task expectedTask = httpTaskManager.getTasks().get(1);

            assertEquals(expectedTask.getTitle(), task.getTitle());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getEpicByIdTest() {
        URI url = URI.create(path + "/tasks/epic?id=7");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            String json = response.body();
            Epic epic = gson.fromJson(json, Epic.class);
            Epic expectedEpic = httpTaskManager.getEpics().get(7);

            assertEquals(expectedEpic.getTitle(), epic.getTitle());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getSubtaskByIdTest() {
        URI url = URI.create(path + "/tasks/subtask?id=5");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            String json = response.body();
            Subtask subtask = gson.fromJson(json, Subtask.class);
            Subtask expectedSubtask = httpTaskManager.getSubtasks().get(5);

            assertEquals(expectedSubtask.getTitle(), subtask.getTitle());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getHistoryTest() {
        URI url = URI.create(path + "/tasks/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());

            String json = response.body();
            Type type = new TypeToken<ArrayList<Task>>(){}.getType();
            List<Task> historyList = gson.fromJson(json, type);
            List<Task> expectedHistoryList = new ArrayList<>(httpTaskManager.getHistory());
            for (int i = 0; i < historyList.size(); i++) {
                assertEquals(expectedHistoryList.get(i).getTitle(), historyList.get(i).getTitle());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getPrioritizedListTest() {
        URI url = URI.create(path + "/tasks/priority");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());

            String json = response.body();
            Type type = new TypeToken<List<Task>>(){}.getType();
            List<Task> prioritizedList = gson.fromJson(json, type);
            List<Task> expectedPrioritizedList = new ArrayList<>(httpTaskManager.getPrioritizedTasks());
            for (int i = 0; i < prioritizedList.size(); i++) {
                assertEquals(expectedPrioritizedList.get(i).getTitle(), prioritizedList.get(i).getTitle());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void sendTaskToServerTest() {
        int id = HTTPTaskManager.getIdCounter();
        Task task = new Task(id, TaskType.TASK, "Test task",
                "test", Status.NEW, LocalDateTime.of(2022, 9, 29, 13, 30, 15), 30);
        URI url = URI.create(path + "/tasks/task");
        String json = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode());
            httpTaskManager.loadFromFile();
            assertEquals(task.getTitle(), httpTaskManager.getTasks().get(id).getTitle());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void sendEpicToServerTest() {
        int id = HTTPTaskManager.getIdCounter();
        Epic epic = new Epic(id, TaskType.EPIC, "Epic epic",
                "test", Status.NEW);
        URI url = URI.create(path + "/tasks/epic");
        String json = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode());
            httpTaskManager.loadFromFile();
            assertEquals(epic.getTitle(), httpTaskManager.getEpics().get(id).getTitle());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void sendSubtaskToServerTest() {
        int id = HTTPTaskManager.getIdCounter();
        Subtask subtask = new Subtask(id, TaskType.SUBTASK, "Test task",
                "test", Status.NEW, 3, LocalDateTime.of(2022, 9, 30, 13, 30, 15), 30);
        URI url = URI.create(path + "/tasks/subtask");
        String json = gson.toJson(subtask);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode());
            httpTaskManager.loadFromFile();
            System.out.println(httpTaskManager.getSubtasks());
            assertEquals(subtask.getTitle(), httpTaskManager.getSubtasks().get(id).getTitle());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void deleteTaskByIdFromServerTest() {
        HTTPTaskManager rollback = Managers.loadedHTTPTasksManager();
        URI url = URI.create(path + "/tasks/task?id=2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            HTTPTaskManager taskManager = Managers.loadedHTTPTasksManager();
            assertNull(taskManager.getTasks().get(2));
            httpTaskManager = rollback;
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void deleteEpicByIdAndAllSubtasksByThisEpicFromServerTest() {
        HTTPTaskManager rollback = Managers.loadedHTTPTasksManager();
        URI url = URI.create(path + "/tasks/epic?id=3");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            httpTaskManager = Managers.loadedHTTPTasksManager();
            assertNull(httpTaskManager.getEpicById(3));
            httpTaskManager = rollback;
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void deleteSubtaskByIdFromServerTest() {
        HTTPTaskManager rollback = Managers.loadedHTTPTasksManager();
        URI url = URI.create(path + "/tasks/subtask?id=6");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            httpTaskManager = Managers.loadedHTTPTasksManager();
            assertNull(httpTaskManager.getSubtasks().get(6));
            httpTaskManager = rollback;
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @AfterAll
    static void restoreFile() {
        var fileManager = Managers.getDefaultFileManager();
        FileBackedTasksManager.setIdCounter(1);
        //создаем объекты и закидываем в файл
        fileManager.add(new Task(FileBackedTasksManager.getIdCounter(), TaskType.TASK, "Продать авто",
                "продать", Status.NEW, LocalDateTime.of(2022, 9, 25, 13, 30, 15), 30));
        fileManager.add(new Task(FileBackedTasksManager.getIdCounter(), TaskType.TASK, "Потратиться на себя",
                "Сделать себе приятно", Status.NEW, LocalDateTime.of(2022, 9, 26, 12, 0, 15), 30));
        fileManager.add(new Epic(FileBackedTasksManager.getIdCounter(), TaskType.EPIC, "Пройти курс Java-разработчик",
                "пройти все спринты", Status.NEW));
        fileManager.add(new Subtask(FileBackedTasksManager.getIdCounter(), TaskType.SUBTASK, "Пройти Java Core",
                "База", Status.NEW, 7, LocalDateTime.now(), 30));
        fileManager.add(new Subtask(FileBackedTasksManager.getIdCounter(), TaskType.SUBTASK, "Стать гуру Spring",
                "Важная задача", Status.NEW, 7, LocalDateTime.of(2022, 9, 24, 10, 0, 15), 45));
        fileManager.add(new Subtask(FileBackedTasksManager.getIdCounter(), TaskType.SUBTASK, "Сдать тесты",
                "Важная задача", Status.NEW, 7, LocalDateTime.of(2022, 9, 25, 11, 0, 15), 120));
        fileManager.add(new Epic(FileBackedTasksManager.getIdCounter(), TaskType.EPIC, "Английский",
                "дойти до уровня Native", Status.NEW));

        fileManager.getTaskById(1);
        fileManager.getEpicById(7);
        fileManager.getEpicById(3);
    }
}
