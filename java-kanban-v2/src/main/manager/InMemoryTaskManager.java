package manager;

import filemanager.ManagerSaveException;
import filemanager.TaskType;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected static int idCounter = 1;
    public final HistoryManager historyManager = Managers.getDefaultHistory();
    protected Map<Integer, Task> tasks = new HashMap<>();
    protected Map<Integer, Epic> epics = new HashMap<>();
    protected Map<Integer, Subtask> subtasks = new HashMap<>();

    protected Set<Task> prioritizedTasks = new TreeSet<>((o1, o2) -> {
        if (o1.getStartTime() == null && o2.getStartTime() == null) return o1.getId() - o2.getId();
        if (o1.getStartTime() == null) return 1;
        if (o2.getStartTime() == null) return -1;
        if (o1.getStartTime().isAfter(o2.getStartTime())) return 1;
        if (o1.getStartTime().isBefore(o2.getStartTime())) return -1;
        if (o1.getStartTime().isEqual(o2.getStartTime())) return o1.getId() - o2.getId();
        return 0;
    });

    @Override
    public Map<Integer, Task> getTasks() {
        return tasks;
    }
    @Override
    public Map<Integer, Epic> getEpics() {
        return epics;
    }
    @Override
    public Map<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public static int getIdCounter() {
        return idCounter;
    }

    public static void setIdCounter(int idCounter) {
        InMemoryTaskManager.idCounter = idCounter;
    }

    @Override
    public void intersectionCheck() {
        LocalDateTime checkTime = null;
        boolean flagCheckTimeIsEmpty = true;
        for (Task task : prioritizedTasks) {
            if (flagCheckTimeIsEmpty) {
                checkTime = task.getEndTime();
                flagCheckTimeIsEmpty = false;
            } else if (task.getStartTime() != null) {
                if (task.getStartTime().isBefore(checkTime)) {
                    throw new ManagerSaveException("Найдено пересечение времени задач, проверьте корректность данных");
                }
                if (task.getStartTime().isAfter(checkTime) || task.getStartTime().isEqual(checkTime)) {
                    checkTime = task.getEndTime();
                }
            }
        }
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        intersectionCheck();
        return prioritizedTasks;
    }

    @Override
    public void getTaskEndTime(Task task) {
        if (task.getStartTime() == null || task.getDuration() == 0) return;
        LocalDateTime endTime = task.getStartTime().plusMinutes(task.getDuration());
        task.setEndTime(endTime);
    }

    @Override
    public void getEpicTimesAndDuration(Epic epic) {
        if (epic.getSubtaskIDs().isEmpty()) {
            return;
        }
        LocalDateTime start;
        LocalDateTime end;
        start = subtasks.get(epic.getSubtaskIDs().get(0)).getStartTime();
        end = subtasks.get(epic.getSubtaskIDs().get(0)).getEndTime();
        epic.setStartTime(start);
        epic.setEndTime(end);
        for (Integer id : epic.getSubtaskIDs()) {
            if (subtasks.get(id).getStartTime() != null && subtasks.get(id).getStartTime().isBefore(start)) {
                start = subtasks.get(id).getStartTime();
            }
            if (subtasks.get(id).getStartTime() != null && subtasks.get(id).getEndTime().isAfter(end)) {
                end = subtasks.get(id).getEndTime();
            }
        }
        epic.setStartTime(start);
        epic.setEndTime(end);
        epic.setDuration(Duration.between(epic.getStartTime(), epic.getEndTime()).toMinutes());
    }

    @Override
    public void getSubtaskEndTime(Subtask subtask) {
        if (subtask.getStartTime() == null || subtask.getDuration() == 0) return;
        LocalDateTime endTime = subtask.getStartTime().plusMinutes(subtask.getDuration());
        subtask.setEndTime(endTime);
        if (epics.containsKey(subtask.getEpicID())) {
            getEpicTimesAndDuration(epics.get(subtask.getEpicID()));
        }
    }

    @Override
    public Task getTaskById(int id) {
        if (!tasks.containsKey(id)) return null;
        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Epic getEpicById(int id) {
        if (!epics.containsKey(id)) return null;
        historyManager.add(epics.get(id));
        return epics.get(id);
    }

    @Override
    public Subtask getSubtaskById(int id) {
        if (!subtasks.containsKey(id)) return null;
        historyManager.add(subtasks.get(id));
        return subtasks.get(id);
    }

    @Override
    public int add(Task task) {
        task.setId(idCounter++);
        getTaskEndTime(task);
        prioritizedTasks.add(task);
        tasks.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public int add(Epic epic) {
        epic.setId(idCounter++);
        epics.put(epic.getId(), epic);
        for (Subtask subtask : subtasks.values()) {
            if(epic.getId() == subtask.getEpicID()) {
                epic.getSubtaskIDs().add(subtask.getId());
            }
        }
        getEpicTimesAndDuration(epic);
        return epic.getId();
    }

    @Override
    public int add(Subtask subtask) {
        subtask.setId(idCounter++);
        getSubtaskEndTime(subtask);
        prioritizedTasks.add(subtask);
        subtasks.put(subtask.getId(), subtask);
        for (Epic epic : epics.values()) {
            if(epic.getId() == subtask.getEpicID()) {
                epic.getSubtaskIDs().add(subtask.getId());
            }
        }
        if (epics.containsKey(subtask.getEpicID())) {
            update(epics.get(subtask.getEpicID()));
            getEpicTimesAndDuration(epics.get(subtask.getEpicID()));
        }
        return subtask.getId();
    }
    @Override
    public int update(Task task) {
        tasks.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public int update(Epic epic) {
        epic.setStatus(Status.NEW);
        epics.put(epic.getId(), epic);
        List<Integer> epicList = epic.getSubtaskIDs();
        if (epic.getSubtaskIDs().isEmpty()) {
            return epic.getId();
        }
        Status status = Status.NEW;
        for (int id : epicList) {
            if (subtasks.get(id).getStatus() == Status.DONE) {
                status = Status.DONE;
            } else {
                status = Status.NEW;
                break;
            }
        }
        if (status == Status.DONE) {
            epic.setStatus(status);
            return epic.getId();
        }
        for (int id : epicList) {
            if (subtasks.get(id).getStatus() == Status.NEW) {
                status = Status.NEW;
            } else {
                status = Status.IN_PROGRESS;
                break;
            }
        }
        epic.setStatus(status);
        return epic.getId();
    }
    @Override
    public int update(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        update(epics.get(subtask.getEpicID()));
        return subtask.getId();
    }

    @Override
    public void removeAllTasks() {
        if (prioritizedTasks.size() > 0) {
            prioritizedTasks.removeIf(task -> task.getType() == TaskType.TASK);
        }
        if (historyManager.getHistory() != null) {
            for (Task task : historyManager.getHistory()) {
                if (task.getType().equals(TaskType.TASK)) {
                    historyManager.removeFromHistory(task);
                }
            }
        }
        tasks.clear();
    }

    @Override
    public void removeAllEpicsAndSubtasks() {
        if (prioritizedTasks.size() > 0) {
            prioritizedTasks.removeIf(task -> task.getType() == TaskType.EPIC || task.getType() == TaskType.SUBTASK);
        }
        if (historyManager.getHistory() != null) {
            for (Task task : historyManager.getHistory()) {
                if (task.getType().equals(TaskType.EPIC) || task.getType().equals(TaskType.SUBTASK)) {
                    historyManager.removeFromHistory(task);
                }
            }
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void removeTaskById(int id) {
        historyManager.removeFromHistory(tasks.get(id));
        tasks.remove(id);
    }

    @Override
    public void removeEpicById(int id) {
        List<Integer> subtasksInEpic = epics.get(id).getSubtaskIDs();
        for (int subtaskId : subtasksInEpic) {
            historyManager.removeFromHistory(subtasks.get(subtaskId));
            subtasks.remove(subtaskId);
        }
        historyManager.removeFromHistory(epics.get(id));
        epics.remove(id);
    }

    @Override
    public void removeSubtaskById(Integer id) {
        int epicID = subtasks.get(id).getEpicID();
        epics.get(epicID).getSubtaskIDs().remove(id);
        update(epics.get(epicID));
        historyManager.removeFromHistory(subtasks.get(id));
        subtasks.remove(id);
    }

    @Override
    public List<Subtask> getAllSubtasksByEpic(int id) {
        List<Integer> numbers = epics.get(id).getSubtaskIDs();
        List<Subtask> subtaskArrayList = new ArrayList<>();
        for (int item : numbers) {
            subtaskArrayList.add(subtasks.get(item));
        }
        return subtaskArrayList;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public String toString() {
        return "TaskManager{" +
                "tasks=" + tasks +
                ", epics=" + epics +
                ", subtasks=" + subtasks +
                '}';
    }
}