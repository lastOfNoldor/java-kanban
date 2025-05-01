import main.model.Epic;
import main.model.Subtask;
import main.model.Task;
import main.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import main.service.TaskManager;
import main.service.FileBackedTaskManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
        task = new Task("Task1", "Test description");
        testManager.createTask(task);
        epic1 = new Epic("Epic1", "Test Epic description");
        testManager.createEpic(epic1);
        subtask1 = new Subtask("Subtask1", "Test Subtask description");
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
    void dataWritesInFileAndLoadsFromItSuccessfully() {
        FileBackedTaskManager taskManager = new FileBackedTaskManager(tempFile);
        task = new Task("Task1", "Test description");
        taskManager.createTask(task);
        epic1 = new Epic("Epic1", "Test Epic description");
        taskManager.createEpic(epic1);
        subtask1 = new Subtask("Subtask1", "Test Subtask description");
        taskManager.createSubTask(subtask1, epic1.getId());
        subtask2 = new Subtask("Subtask2", "Test Subtask description");
        taskManager.createSubTask(subtask2, epic1.getId());
        epic2 = new Epic("Epic2", "Test Epic description");
        taskManager.createEpic(epic2);

        FileBackedTaskManager testManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(1, testManager.getRegularTasksList().size());
        assertEquals(5, testManager.getRegularTasksList().size() + testManager.getSubTasksTasksList().size() + testManager.getEpicTasksList().size());
        assertEquals(2, testManager.getEpicById(epic1.getId()).getEpicSubtasks().size());
        System.out.println(testManager.getEpicById(epic1.getId()).getEpicSubtasks().size());
    }
}