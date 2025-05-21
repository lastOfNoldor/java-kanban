package main.test;


import main.model.Epic;
import main.model.Subtask;
import main.model.Task;
import main.model.TaskStatus;
import main.service.FileBackedTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File tempFile;

    @BeforeEach
    void init() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();
        taskManager = FileBackedTaskManager.loadFromFile(tempFile);
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

    @Test
    void workWithEmptyListAtFirstStart() throws IOException {
        File tempFile2 = File.createTempFile("tasks", ".csv");
        tempFile2.deleteOnExit();
        FileBackedTaskManager testManager = FileBackedTaskManager.loadFromFile(tempFile2);
        assertEquals(0, testManager.getRegularTasksList().size());
        task = new Task("Task1", "Test description", LocalDateTime.of(2025, Month.DECEMBER, 9, 12, 12), Duration.ofHours(1));
        testManager.createTask(task);
        epic1 = new Epic("Epic1", "Test Epic description");
        testManager.createEpic(epic1);
        subtask1 = new Subtask("Subtask1", "Test Subtask description", LocalDateTime.of(2023, Month.DECEMBER, 12, 14, 12), Duration.ofHours(1));
        testManager.createSubTask(subtask1, epic1.getId());
        subtask1.setTaskStatus(TaskStatus.DONE);
        testManager.updateSubTask(subtask1);
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            while (reader.ready()) {
                System.out.println(reader.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(3, testManager.getRegularTasksList().size() + testManager.getSubTasksTasksList().size() + testManager.getEpicTasksList().size());
    }

    @Test
    void dataWritesInFileAndLoadsFromItSuccessfullyAndNextTaskIdWritesProperly() {
        FileBackedTaskManager testManager = new FileBackedTaskManager(tempFile);
        task = new Task("Task1", "Test description", LocalDateTime.of(2025, Month.DECEMBER, 13, 12, 12), Duration.ofHours(1));
        testManager.createTask(task);
        epic1 = new Epic("Epic1", "Test Epic description");
        testManager.createEpic(epic1);
        subtask1 = new Subtask("Subtask1", "Test Subtask description", LocalDateTime.of(2025, Month.DECEMBER, 14, 12, 12), Duration.ofHours(1));
        testManager.createSubTask(subtask1, epic1.getId());
        subtask2 = new Subtask("Subtask2", "Test Subtask description", LocalDateTime.of(2025, Month.DECEMBER, 15, 12, 12), Duration.ofHours(1));
        testManager.createSubTask(subtask2, epic1.getId());
        epic2 = new Epic("Epic2", "Test Epic description");
        testManager.createEpic(epic2);
        subtask1.setTaskStatus(TaskStatus.DONE);
        testManager.updateSubTask(subtask1);
        testManager.printAllTasks();

        FileBackedTaskManager newTestManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(1, newTestManager.getRegularTasksList().size());
        assertEquals(5, newTestManager.getRegularTasksList().size() + newTestManager.getSubTasksTasksList().size() + newTestManager.getEpicTasksList().size());
        newTestManager.printAllTasks();
        assertEquals(2, newTestManager.getEpicById(epic1.getId()).get().getEpicSubtasks().size());
        System.out.println(newTestManager.getEpicById(epic1.getId()).get().getEpicSubtasks().size());
        Subtask subtaskAfterLoad = new Subtask("Subtask after load", "Test Subtask description", LocalDateTime.of(2025, Month.DECEMBER, 16, 12, 12), Duration.ofHours(1));
        newTestManager.createSubTask(subtaskAfterLoad, epic2.getId());
        assertEquals(6, subtaskAfterLoad.getId());
        assertEquals(1, newTestManager.getEpicById(epic2.getId()).get().getEpicSubtasks().size());
        newTestManager.printAllTasks();
    }


}