package filemanager;

import manager.HistoryManager;
import tasks.Subtask;
import tasks.Task;

import java.util.List;
import java.util.Map;

public class Formatter {
    public String historyToString(HistoryManager historyManager) {
        if (historyManager.getHistory() == null) return "";
        List<Task> historyList = historyManager.getHistory();
        StringBuilder sb = new StringBuilder();
        for (Task task : historyList) {
            sb.append(task.getId()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public String tasksToString(Map<Integer, Task> allTasks) {
        StringBuilder sb = new StringBuilder();
        for (Task task : allTasks.values()) {
            if (task.getType() == TaskType.TASK || task.getType() == TaskType.EPIC) {
                sb.append(task.getId()).append(",").append(task.getType()).append(",").append(task.getTitle())
                        .append(",").append(task.getDescription()).append(",").append(task.getStatus()).append(",")
                        .append(task.getDuration()).append(",")
                        .append(task.getStartTime()).append(",")
                        .append(task.getEndTime()).append("\n");
            } else {
                Subtask subtask = (Subtask) task;
                sb.append(task.getId()).append(",").append(task.getType()).append(",").append(task.getTitle())
                        .append(",").append(task.getDescription()).append(",").append(task.getStatus()).append(",")
                        .append(task.getDuration()).append(",").append(task.getStartTime()).append(",")
                        .append(task.getEndTime()).append(",").append(subtask.getEpicID()).append("\n");
            }
        }
        return sb.toString();
    }
}
