package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TaskManager {
    Map<Integer, Task> getTasks();
    Map<Integer, Epic> getEpics();
    Map<Integer, Subtask> getSubtasks();

    Task getTaskById(int id);
    Epic getEpicById(int id);
    Subtask getSubtaskById(int id);

    int add(Task task);
    int add(Epic epic);
    int add(Subtask subtask);
    int update(Task task);
    int update(Epic epic);
    int update(Subtask subtask);

    void removeAllTasks();
    void removeAllEpicsAndSubtasks();
    void removeTaskById(int id);
    void removeEpicById(int id);
    void removeSubtaskById(Integer id);

    List<Subtask> getAllSubtasksByEpic(int id);

    List<Task> getHistory();

    void intersectionCheck();

    Set<Task> getPrioritizedTasks();

    void getTaskEndTime(Task task);

    void getEpicTimesAndDuration(Epic epic);

    void getSubtaskEndTime(Subtask subtask);

}
