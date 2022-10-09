package tasks;

import filemanager.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    protected int epicID;

    public Subtask(int id, TaskType type, String title, String description, Status status, int epicID, LocalDateTime startTime, long duration) {
        super(id, type, title, description, status, startTime, duration);
        this.epicID = epicID;
    }

    public int getEpicID() {
        return epicID;
    }

    public void setEpicID(int epicID) {
        this.epicID = epicID;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicID=" + epicID +
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
}
