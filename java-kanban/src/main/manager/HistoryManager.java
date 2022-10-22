package manager;

import tasks.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);
    void removeFromHistory(Task task);
    List<Task> getHistory();

}
