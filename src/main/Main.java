package main;

import main.model.Epic;
import main.model.Subtask;
import main.model.TaskStatus;
import main.service.TaskManager;
import main.model.Task;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        Task task1 = new Task("Обычное задание#1", "description");
        Task task2 = new Task("Обычное задание#2", "description");
        Task task3 = new Task("Обычное задание#3", "description");
        Epic et1 = new Epic("Эпик#1", "description");
        Subtask subtask1 = new Subtask("Подзадача первого Эпика #1", "description", et1.getId());
        Epic et2 = new Epic("Эпик#2", "description");
        Subtask subtask2 = new Subtask("Подзадача второго Эпика #1", "description", et2.getId());
        Subtask subtask3 = new Subtask("Подзадача второго Эпика #2", "description", et2.getId());

        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);
        taskManager.createEpic(et1);
        taskManager.createEpic(et2);
        taskManager.createSubTask(subtask1);
        taskManager.createSubTask(subtask2);
        taskManager.createSubTask(subtask3);

        subtask1.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask(subtask1);

        System.out.println(et1);
        System.out.println(task2);
        System.out.println(subtask1);

    }
}
