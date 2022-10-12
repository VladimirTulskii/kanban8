package filemanager;

import manager.TaskManagerTest;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {

    @Override
    public FileBackedTasksManager createManager() {
        FileBackedTasksManager.setIdCounter(1);
        return new FileBackedTasksManager();
    }

    @Test
    void loadedFromFileTasksManagerTest() {
        Epic epic = new Epic(1, TaskType.EPIC, "new epic", "test description", Status.NEW);
        Subtask s1 = new Subtask(2, TaskType.SUBTASK, "subtask 1", "test description", Status.NEW,
                1, LocalDateTime.of(2022, 9, 26, 20, 0), 30);
        Subtask s2 = new Subtask(3, TaskType.SUBTASK, "subtask 2", "test description", Status.NEW,
                1, LocalDateTime.of(2022, 9, 26, 18, 0), 30);
        Subtask s3 = new Subtask(4, TaskType.SUBTASK, "subtask 3", "test description", Status.NEW,
                1, LocalDateTime.of(2022, 9, 26, 19, 0), 45);
        manager.add(epic);
        manager.add(s1);
        manager.add(s2);
        manager.add(s3);

        manager.getEpicById(1);
        manager.getSubtaskById(4);
        manager.getEpicById(1);
        manager.getSubtaskById(2);

        String[] lines;
        final String PATH = "resources/data.csv";
        try {
            lines = Files.readString(Path.of(PATH)).split("\n");
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось считать файл");
        }
        assertEquals(7, lines.length);

        FileBackedTasksManager fileManagerFromFile = FileBackedTasksManager.loadedFromFileTasksManager();

        Integer[] forComparison = {s3.getId(), epic.getId(), s1.getId()};
        List<Task> history = fileManagerFromFile.getHistory();
        Integer[] historyFromFile = {history.get(0).getId(), history.get(1).getId(), history.get(2).getId()};

        assertEquals("new epic", fileManagerFromFile.getEpics().get(1).getTitle());
        assertEquals("subtask 1", fileManagerFromFile.getSubtasks().get(2).getTitle());
        assertEquals("subtask 2", fileManagerFromFile.getSubtasks().get(3).getTitle());
        assertEquals("subtask 3", fileManagerFromFile.getSubtasks().get(4).getTitle());
        assertArrayEquals(forComparison, historyFromFile);
    }

    @Test
    void loadWithEmptyTasksListAndEmptyHistoryListInFileTest() {
        final String PATH = "resources/data.csv";
        try (Writer writer = new FileWriter(PATH)) {
            String stringToFile = "";
            writer.write(stringToFile);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка в работе менеджера");
        }
        manager = FileBackedTasksManager.loadedFromFileTasksManager();
        assertTrue(manager.getTasks().isEmpty());
        assertTrue(manager.getEpics().isEmpty());
        assertTrue(manager.getSubtasks().isEmpty());
        assertNull(manager.getHistory());
    }

    @Test
    void loadWithEpicWithoutSubtasks() {
        Epic epic = new Epic(1, TaskType.EPIC, "new epic", "test description", Status.NEW);
        manager.add(epic);

        FileBackedTasksManager fileManager = FileBackedTasksManager.loadedFromFileTasksManager();

        assertTrue(fileManager.getEpics().containsKey(1));
        assertEquals("new epic", fileManager.getEpics().get(1).getTitle());
    }

}