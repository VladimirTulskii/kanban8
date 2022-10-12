package manager;

import filemanager.TaskType;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Task;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    public InMemoryTaskManager createManager() {
        InMemoryTaskManager.setIdCounter(1);
        return new InMemoryTaskManager();
    }

    @Test
    void getIdCounterTest() {
        manager.add(new Task(1, TaskType.TASK, "Task1", "test description", Status.NEW,
                LocalDateTime.of(2022, 9, 26, 18, 0), 30));
        manager.add(new Task(2, TaskType.TASK, "Task2", "test description", Status.NEW,
                LocalDateTime.of(2022, 9, 26, 18, 0), 30));
        manager.add(new Epic(3, TaskType.EPIC, "new epic", "test description", Status.NEW));

        assertEquals(4, InMemoryTaskManager.getIdCounter());
    }

    @Test
    void setIdCounterTest() {
        InMemoryTaskManager.setIdCounter(8);

        assertEquals(8, InMemoryTaskManager.getIdCounter());
    }
}