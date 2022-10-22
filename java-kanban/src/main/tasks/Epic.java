package tasks;

import filemanager.TaskType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    protected List<Integer> subtaskIDs;

    public Epic(int id, TaskType type, String title, String description, Status status) {
        super(id, type, title, description, status, null, 0);
        subtaskIDs = new ArrayList<>();
    }

    public List<Integer> getSubtaskIDs() {
        return subtaskIDs;
    }

    public void setSubtaskIDs(List<Integer> subtaskIDs) {
        this.subtaskIDs = subtaskIDs;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "subtaskIDs=" + subtaskIDs +
                ", id=" + id +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtaskIDs, epic.subtaskIDs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskIDs);
    }
}
