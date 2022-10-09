package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    public final CustomLinkedList<Task> historyList = new CustomLinkedList<>();
    private final HashMap<Integer, Node<Task>> nodeMap = new HashMap<>();

    @Override
    public void add(Task task) {
        removeFromHistory(task);
        historyList.addLast(task);
        nodeMap.put(task.getId(), historyList.tail.prev);
    }

    public void add(Epic epic) {
        removeFromHistory(epic);
        historyList.addLast(epic);
        nodeMap.put(epic.getId(), historyList.tail.prev);
    }

    public void add(Subtask subtask) {
        removeFromHistory(subtask);
        historyList.addLast(subtask);
        nodeMap.put(subtask.getId(), historyList.tail.prev);
    }

    @Override
    public void removeFromHistory(Task task) {
        if (nodeMap.containsKey(task.getId())) {
            historyList.removeNode(nodeMap.get(task.getId()));
            nodeMap.remove(task.getId());
        }
    }

    @Override
    public List<Task> getHistory() {
        if (nodeMap.isEmpty()) return null;
        return historyList.getTasks();
    }
}
