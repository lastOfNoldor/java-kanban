package main;


import main.model.Epic;
import main.model.Subtask;
import main.model.Task;
import main.model.TaskStatus;
import main.service.InMemoryTaskManager;
import main.service.Managers;
import main.service.TaskManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();
        Epic et2 = new Epic("Эпик#2", "description");
        Subtask subtask2 = new Subtask("Первая Подзадача второго Эпика", "description");
        Subtask subtask3 = new Subtask("Вторая Подзадача второго Эпика", "description");
        taskManager.createEpic(et2);
        taskManager.createSubTask(subtask2, et2.getId());
        taskManager.createSubTask(subtask3, et2.getId());
        assertEquals(TaskStatus.NEW, et2.getTaskStatus());
        taskManager.getEpicById(et2.getId());
        System.out.println(taskManager.getHistoryManager().getHistory());
        subtask2.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subtask2);
        taskManager.getEpicById(et2.getId());
        System.out.println(taskManager.getHistoryManager().getHistory());
    }
}
