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

public class FileBackedTaskManagerTest {

    private File tempFile;
    private Task task;
    private Epic epic1;
    private Subtask subtask1;
    private Subtask subtask2;
    private Epic epic2;

    @BeforeEach
    void init() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();

    }

    @Test
    void workWithEmptyListAtFirstStart() {
        FileBackedTaskManager testManager = FileBackedTaskManager.loadFromFile(tempFile);
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
        FileBackedTaskManager taskManager = new FileBackedTaskManager(tempFile);
        task = new Task("Task1", "Test description", LocalDateTime.of(2025, Month.DECEMBER, 13, 12, 12), Duration.ofHours(1));
        taskManager.createTask(task);
        epic1 = new Epic("Epic1", "Test Epic description");
        taskManager.createEpic(epic1);
        subtask1 = new Subtask("Subtask1", "Test Subtask description", LocalDateTime.of(2025, Month.DECEMBER, 14, 12, 12), Duration.ofHours(1));
        taskManager.createSubTask(subtask1, epic1.getId());
        subtask2 = new Subtask("Subtask2", "Test Subtask description", LocalDateTime.of(2025, Month.DECEMBER, 15, 12, 12), Duration.ofHours(1));
        taskManager.createSubTask(subtask2, epic1.getId());
        epic2 = new Epic("Epic2", "Test Epic description");
        taskManager.createEpic(epic2);
        taskManager.printAllTasks();

        FileBackedTaskManager testManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(1, testManager.getRegularTasksList().size());
        assertEquals(5, testManager.getRegularTasksList().size() + testManager.getSubTasksTasksList().size() + testManager.getEpicTasksList().size());
        testManager.printAllTasks();
        assertEquals(2, testManager.getEpicById(epic1.getId()).getEpicSubtasks().size());
        System.out.println(testManager.getEpicById(epic1.getId()).getEpicSubtasks().size());
        Subtask subtaskAfterLoad = new Subtask("Subtask after load", "Test Subtask description", LocalDateTime.of(2025, Month.DECEMBER, 16, 12, 12), Duration.ofHours(1));
        testManager.createSubTask(subtaskAfterLoad, epic1.getId());
        assertEquals(6, subtaskAfterLoad.getId());
        assertEquals(3, testManager.getEpicById(epic1.getId()).getEpicSubtasks().size());
        testManager.printAllTasks();
    }

}