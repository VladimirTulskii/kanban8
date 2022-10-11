import filemanager.FileBackedTasksManager;
import filemanager.TaskType;
import manager.Managers;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.time.LocalDateTime;

public class Main {
    //проверочный main
    public static void main(String[] args) {
        var fileManager = Managers.getDefaultFileManager();
        //создаем объекты и закидываем в файл
        fileManager.add(new Task(FileBackedTasksManager.getIdCounter(), TaskType.TASK, "Продать авто",
                "продать", Status.NEW, LocalDateTime.of(2022, 9, 25, 13, 30, 15), 30));
        fileManager.add(new Task(FileBackedTasksManager.getIdCounter(), TaskType.TASK, "Потратиться на себя",
                "Сделать себе приятно", Status.NEW, LocalDateTime.of(2022, 9, 26, 12, 0, 15), 30));
        fileManager.add(new Epic(FileBackedTasksManager.getIdCounter(), TaskType.EPIC, "Пройти курс Java-разработчик",
                "пройти все спринты", Status.NEW));
        fileManager.add(new Subtask(FileBackedTasksManager.getIdCounter(), TaskType.SUBTASK, "Пройти Java Core",
                "База", Status.NEW, 7, LocalDateTime.now(), 30));
        fileManager.add(new Subtask(FileBackedTasksManager.getIdCounter(), TaskType.SUBTASK, "Стать гуру Spring",
                "Важная задача", Status.NEW, 7, LocalDateTime.of(2022, 9, 24, 10, 0, 15), 45));
        fileManager.add(new Subtask(FileBackedTasksManager.getIdCounter(), TaskType.SUBTASK, "Сдать тесты",
                "Важная задача", Status.NEW, 7, LocalDateTime.of(2022, 9, 25, 11, 0, 15), 120));
        fileManager.add(new Epic(FileBackedTasksManager.getIdCounter(), TaskType.EPIC, "Английский",
                "дойти до уровня Native", Status.NEW));

        fileManager.getTaskById(1);
        fileManager.getEpicById(7);
        fileManager.getEpicById(3);


        System.out.println("Sorted by start time------------------");
        System.out.println(fileManager.getPrioritizedTasks());

        var fileManager1 = FileBackedTasksManager.loadedFromFileTasksManager();
        System.out.println(("All tasks------------------"));


        for (Task task : fileManager1.getTasks().values()) {
            System.out.println(task);
        }
        for (Epic epic : fileManager1.getEpics().values()) {
            System.out.println(epic);
        }
        for (Subtask subtask : fileManager1.getSubtasks().values()) {
            System.out.println(subtask);
        }

        System.out.println(("Sorted from file by start time------------------"));
        fileManager1.getPrioritizedTasks();
    }
}