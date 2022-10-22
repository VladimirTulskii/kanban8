package httpserver;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import filemanager.TaskType;
import httpclient.HttpTaskManager;
import kvserver.KVServer;
import manager.Managers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ServersTest {
    HttpTaskManager httpTaskManager = Managers.getDefaultHttpTaskManager("http://localhost:8078", false);
    KVServer kvServer;
    HttpTaskServer httpTaskServer = new HttpTaskServer();
    Gson gson = Managers.getGson();

    Task task1 = new Task(1, TaskType.TASK, "Продать авто",
            "продать", Status.NEW, LocalDateTime.of(2022, 9, 25, 13, 30, 15), 30);
    Task task2 = new Task(2, TaskType.TASK, "Потратиться на себя",
            "Сделать себе приятно", Status.NEW, LocalDateTime.of(2022, 9, 26, 12, 0, 15), 30);
    Epic epic1 = new Epic(3, TaskType.EPIC, "Пройти курс Java-разработчик",
            "пройти все спринты", Status.NEW);
    Subtask subtask1 = new Subtask(4, TaskType.SUBTASK, "Пройти Java Core",
            "База", Status.NEW, 7, LocalDateTime.now(), 30);
    Subtask subtask2 = new Subtask(5, TaskType.SUBTASK, "Стать гуру Spring",
            "Важная задача", Status.NEW, 7, LocalDateTime.of(2022, 9, 24, 10, 0, 15), 45);
    Subtask subtask3 = new Subtask(6, TaskType.SUBTASK, "Сдать тесты",
            "Важная задача", Status.NEW, 7, LocalDateTime.of(2022, 9, 25, 11, 0, 15), 120);
    Epic epic2 = new Epic(7, TaskType.EPIC, "Английский",
            "дойти до уровня Native", Status.NEW);
    public ServersTest() throws IOException {
    }

    @BeforeEach
    void start() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
        httpTaskServer.start();
    }

    @AfterEach
    void stop() {
        kvServer.stop();
        httpTaskServer.stop();
    }

    @Test
    void sendAndLoadAllTasksAndHistoryKVServer() {
        httpTaskManager.getToken();
        task1.setEndTime(task1.getStartTime().plusMinutes(task1.getDuration()));
        task2.setEndTime(task2.getStartTime().plusMinutes(task2.getDuration()));
        subtask1.setEndTime(subtask1.getStartTime().plusMinutes(subtask1.getDuration()));
        subtask2.setEndTime(subtask2.getStartTime().plusMinutes(subtask2.getDuration()));
        subtask3.setEndTime(subtask3.getStartTime().plusMinutes(subtask3.getDuration()));
        httpTaskManager.add(task1);
        httpTaskManager.add(task2);
        httpTaskManager.add(epic1);
        httpTaskManager.add(subtask1);
        httpTaskManager.add(subtask2);
        httpTaskManager.add(subtask3);
        httpTaskManager.add(epic2);
        httpTaskManager.getTaskById(1);
        httpTaskManager.getSubtaskById(5);
        httpTaskManager.save();
        HttpTaskManager loadTM = Managers.getDefaultHttpTaskManager("http://localhost:8078", true);
        assertEquals(httpTaskManager.getTasks(), loadTM.getTasks());
        assertEquals(httpTaskManager.getEpics(), loadTM.getEpics());
        assertEquals(httpTaskManager.getSubtasks(), loadTM.getSubtasks());
        assertEquals(httpTaskManager.getHistory(), loadTM.getHistory());
        assertEquals(httpTaskManager.getPrioritizedTasks(), loadTM.getPrioritizedTasks());

    }

    @Test
    void sendAndLoadTasksKVServer() {
        httpTaskManager.getToken();
        httpTaskManager.add(epic1);
        httpTaskManager.save();
        HttpTaskManager loadTM = Managers.getDefaultHttpTaskManager("http://localhost:8078", true);
        assertEquals(httpTaskManager.getEpics(), loadTM.getEpics());
    }

    @Test
    void sendAndLoadSubtasksKVServer() {
        httpTaskManager.getToken();
        httpTaskManager.add(subtask1);
        httpTaskManager.save();
        HttpTaskManager loadTM = Managers.getDefaultHttpTaskManager("http://localhost:8078", true);
        assertEquals(httpTaskManager.getSubtasks(), loadTM.getSubtasks());
    }

    @Test
    void getPrioritizedTasksListTest() {
        httpTaskManager.getToken();
        task1.setEndTime(task1.getStartTime().plusMinutes(task1.getDuration()));
        task2.setEndTime(task2.getStartTime().plusMinutes(task2.getDuration()));
        subtask1.setEndTime(subtask1.getStartTime().plusMinutes(subtask1.getDuration()));
        subtask2.setEndTime(subtask2.getStartTime().plusMinutes(subtask2.getDuration()));
        subtask3.setEndTime(subtask3.getStartTime().plusMinutes(subtask3.getDuration()));
        httpTaskManager.add(task1);
        httpTaskManager.add(task2);
        httpTaskManager.add(epic1);
        httpTaskManager.add(subtask1);
        httpTaskManager.add(subtask2);
        httpTaskManager.add(subtask3);
        httpTaskManager.save();
        HttpTaskManager loadTM = Managers.getDefaultHttpTaskManager("http://localhost:8078", true);
        assertEquals(httpTaskManager.getPrioritizedTasks(), loadTM.getPrioritizedTasks());
    }

    @Test
    void sendAndLoadEpicsKVServer() {
        httpTaskManager.getToken();
        httpTaskManager.add(task1);
        httpTaskManager.add(task2);
        httpTaskManager.save();
        HttpTaskManager loadTM = Managers.getDefaultHttpTaskManager("http://localhost:8078", true);
        assertEquals(httpTaskManager.getTasks(), loadTM.getTasks());
    }

    @Test
    void sendTaskToHttpServerTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/task");
        task1.setEndTime(task1.getStartTime().plusMinutes(30));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        Task task = HttpTaskServer.getHttpTaskManager().getTasks().get(1);
        assertEquals(task, task1);
    }

    @Test
    void sendEpicToHttpServerTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/epic");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic1)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        Epic epic = HttpTaskServer.getHttpTaskManager().getEpics().get(3);
        assertEquals(epic, epic1);
    }

    @Test
    void sendSubtaskToHttpServerTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        subtask1.setEndTime(subtask1.getStartTime().plusMinutes(30));
        URI uri = URI.create("http://localhost:8080/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask1)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        Subtask subtask = HttpTaskServer.getHttpTaskManager().getSubtasks().get(4);
        assertEquals(subtask, subtask1);
    }

    @Test
    void getTaskByIdTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/task");
        task1.setEndTime(task1.getStartTime().plusMinutes(30));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1)))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        //----------------------------------------------------------------------

        URI url = URI.create("http://localhost:8080/tasks/task?id=1");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request1, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            String json = response.body();
            Task task = gson.fromJson(json, Task.class);
            assertEquals(task1, task);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getEpicByIdTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/epic");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic1)))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        //----------------------------------------------------------------------

        URI url = URI.create("http://localhost:8080/tasks/epic?id=3");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request1, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            String json = response.body();
            Epic epic = gson.fromJson(json, Epic.class);
            assertEquals(epic1, epic);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getSubtaskByIdTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        subtask1.setEndTime(subtask1.getStartTime().plusMinutes(subtask1.getDuration()));
        URI uri = URI.create("http://localhost:8080/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask1)))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        //----------------------------------------------------------------------

        URI url = URI.create("http://localhost:8080/tasks/subtask?id=4");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request1, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            String json = response.body();
            Subtask subtask = gson.fromJson(json, Subtask.class);
            assertEquals(subtask1, subtask);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getPriorityTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/task");
        task1.setEndTime(task1.getStartTime().plusMinutes(30));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1)))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
        uri = URI.create("http://localhost:8080/tasks/task");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task2)))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
        httpTaskManager.add(task1);
        httpTaskManager.add(task2);

        //---------------------------------------------------------------------

        URI url = URI.create("http://localhost:8080/tasks/priority");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request1, HttpResponse.BodyHandlers.ofString());
            String json = response.body();
            Type type = new TypeToken<List<Task>>(){}.getType();

            List<Task> prioritizedList = gson.fromJson(json, type);

            List<Task> expectedPrioritizedList = new ArrayList<>(httpTaskManager.getPrioritizedTasks());
            assertEquals(prioritizedList, expectedPrioritizedList);
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void getAllTasksEpicsSubtasksTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/task");
        task1.setEndTime(task1.getStartTime().plusMinutes(30));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1)))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
        uri = URI.create("http://localhost:8080/tasks/epic");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic1)))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
        uri = URI.create("http://localhost:8080/tasks/subtask");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask1)))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        httpTaskManager.add(task1);
        httpTaskManager.add(epic1);
        httpTaskManager.add(subtask1);

        //------------------------------------------------------------------------

        URI url = URI.create("http://localhost:8080/tasks");
        request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            String json = response.body();
            Type type = new TypeToken<ArrayList<Task>>(){}.getType();
            List<Task> tasksList = gson.fromJson(json, type);
            assertEquals(3, tasksList.size());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void deleteTaskByIdTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/task");
        task1.setEndTime(task1.getStartTime().plusMinutes(30));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1)))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        //--------------------------------------------------------------------

        URI url = URI.create("http://localhost:8080/tasks/task?id=1");
        request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertNull(HttpTaskServer.getHttpTaskManager().getTasks().get(1));
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void deleteEpicAndSubtasksByEpicIdTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        subtask1.setEndTime(subtask1.getStartTime().plusMinutes(subtask1.getDuration()));
        URI uri = URI.create("http://localhost:8080/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask1)))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
        uri = URI.create("http://localhost:8080/tasks/epic");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic2)))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        //--------------------------------------------------------------------

        URI url = URI.create("http://localhost:8080/tasks/epic?id=7");
        request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertNull(HttpTaskServer.getHttpTaskManager().getEpics().get(7));
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void deleteSubtaskByIdTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        subtask1.setEndTime(subtask1.getStartTime().plusMinutes(subtask1.getDuration()));
        URI uri = URI.create("http://localhost:8080/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask1)))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        //-----------------------------------------------------------------------

        URI url = URI.create("http://localhost:8080/tasks/subtask?id=4");
        request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertNull(HttpTaskServer.getHttpTaskManager().getSubtasks().get(4));
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void deleteAllTasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/task");
        task1.setEndTime(task1.getStartTime().plusMinutes(task1.getDuration()));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1)))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
        task2.setEndTime(task2.getStartTime().plusMinutes(task2.getDuration()));
        uri = URI.create("http://localhost:8080/tasks/task");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task2)))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        //-----------------------------------------------------------------------

        URI url = URI.create("http://localhost:8080/tasks/task");
        request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertEquals("{}", HttpTaskServer.getHttpTaskManager().getTasks().toString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void deleteAllEpicsAndSubtasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/task");
        task1.setEndTime(task1.getStartTime().plusMinutes(task1.getDuration()));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1)))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
        task2.setEndTime(task2.getStartTime().plusMinutes(task2.getDuration()));
        uri = URI.create("http://localhost:8080/tasks/task");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task2)))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
        subtask1.setEndTime(subtask1.getStartTime().plusMinutes(subtask1.getDuration()));
        uri = URI.create("http://localhost:8080/tasks/subtask");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask1)))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
        uri = URI.create("http://localhost:8080/tasks/epic");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic2)))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        //-----------------------------------------------------------------------

        URI url = URI.create("http://localhost:8080/tasks/epic");
        request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertEquals("{}", HttpTaskServer.getHttpTaskManager().getEpics().toString());
            assertEquals("{}", HttpTaskServer.getHttpTaskManager().getSubtasks().toString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

}
