package main.test;

import main.model.Epic;
import main.model.Subtask;
import main.model.Task;
import main.service.InMemoryTaskManager;
import main.service.Managers;
import org.junit.jupiter.api.BeforeEach;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;


class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    void init() {
        taskManager = Managers.getDefault();
        task = new Task("Task1", "Test addNewTask description", LocalDateTime.of(2025, Month.DECEMBER, 9, 12, 12), Duration.ofHours(1));
        epic1 = new Epic("Эпик#1", "description");
        subtask1 = new Subtask("Подзадача первого Эпика", "description", LocalDateTime.of(2026, Month.DECEMBER, 9, 12, 12), Duration.ofHours(1));
        epic2 = new Epic("Эпик#2", "description");
        subtask2 = new Subtask("Первая Подзадача второго Эпика", "description", LocalDateTime.of(2025, Month.DECEMBER, 13, 12, 12), Duration.ofHours(1));
        subtask3 = new Subtask("Вторая Подзадача второго Эпика", "description", LocalDateTime.of(2025, Month.DECEMBER, 12, 12, 12), Duration.ofHours(1));
        taskManager.createTask(task);
        taskManager.createEpic(epic1);
        taskManager.createSubTask(subtask1, epic1.getId());
        taskManager.createEpic(epic2);
        taskManager.createSubTask(subtask2, epic2.getId());
        taskManager.createSubTask(subtask3, epic2.getId());
    }

}
