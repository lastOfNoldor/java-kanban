package main.test;

import main.model.Epic;
import main.model.Subtask;
import main.model.Task;
import main.service.Managers;
import main.service.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryHistoryManagerTest {

    TaskManager taskManager;
    Task task;
    Task task2;
    Task task3;
    Task task4;

    @BeforeEach
    void init() {
        taskManager = Managers.getDefault();
        task = new Task("Test1", "Test addNewTask description", LocalDateTime.of(2025, Month.DECEMBER, 9, 12, 12), Duration.ofHours(1));
        taskManager.createTask(task);
        task2 = new Task("Test2", "Test addNewTask description", LocalDateTime.of(2025, Month.DECEMBER, 12, 12, 12), Duration.ofHours(1));
        taskManager.createTask(task2);
        task3 = new Task("Test3", "Test addNewTask description", LocalDateTime.of(2025, Month.DECEMBER, 13, 14, 12), Duration.ofHours(1));
        taskManager.createTask(task3);
        task4 = new Task("Test4", "Test addNewTask description", LocalDateTime.of(2025, Month.DECEMBER, 14, 12, 12), Duration.ofHours(1));
        taskManager.createTask(task4);
        taskManager.getTaskById(task.getId()).orElseThrow();
        taskManager.getTaskById(task2.getId()).orElseThrow();
        taskManager.getTaskById(task3.getId()).orElseThrow();
        taskManager.getTaskById(task4.getId()).orElseThrow();
    }


    @Test
    void linkedListWorks() {
        assertEquals(task.getId(), taskManager.getHistoryManager().getHistory().getFirst().getId());
        assertEquals(task4.getId(), taskManager.getHistoryManager().getHistory().getLast().getId());
        assertEquals(task2.getId(), taskManager.getHistoryManager().getHistory().get(1).getId());

    }

    @Test
    void noDuplicateInHistory() {
        assertEquals(task.getId(), taskManager.getHistoryManager().getHistory().getFirst().getId());
        assertEquals(task4.getId(), taskManager.getHistoryManager().getHistory().getLast().getId());
        taskManager.getTaskById(task.getId()).orElseThrow();
        assertEquals(task2.getId(), taskManager.getHistoryManager().getHistory().getFirst().getId());
        assertEquals(task.getId(), taskManager.getHistoryManager().getHistory().getLast().getId());
        assertEquals(task4.getId(), taskManager.getHistoryManager().getHistory().get(2).getId());
        taskManager.getTaskById(task3.getId()).orElseThrow();
        assertEquals(task4.getId(), taskManager.getHistoryManager().getHistory().get(1).getId());
        assertEquals(task.getId(), taskManager.getHistoryManager().getHistory().get(2).getId());
        assertEquals(task3.getId(), taskManager.getHistoryManager().getHistory().getLast().getId());
    }

    @Test
    void removeInHistoryWorksAndLinkedListStaysFine() {
        assertEquals(task2.getId(), taskManager.getHistoryManager().getHistory().get(1).getId());
        assertEquals(task3.getId(), taskManager.getHistoryManager().getHistory().get(2).getId());
        taskManager.getHistoryManager().remove(task2.getId());
        assertEquals(task3.getId(), taskManager.getHistoryManager().getHistory().get(1).getId());
        assertEquals(task4.getId(), taskManager.getHistoryManager().getHistory().get(2).getId());
    }

    @Test
    void subtasksAndEpicWorksFineInHistory() {
        Epic epic = new Epic("Epic1", "Ep Description");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Sub Description", LocalDateTime.of(2025, Month.DECEMBER, 15, 12, 12), Duration.ofHours(1));
        taskManager.createSubtask(subtask, epic.getId());
        taskManager.getSubtaskById(subtask.getId()).orElseThrow();
        assertEquals(task4.getId(), taskManager.getHistoryManager().getHistory().get(3).getId());
        assertEquals(subtask.getId(), taskManager.getHistoryManager().getHistory().getLast().getId());
        taskManager.getEpicById(epic.getId()).orElseThrow();
        assertEquals(task4.getId(), taskManager.getHistoryManager().getHistory().get(3).getId());
        assertEquals(subtask.getId(), taskManager.getHistoryManager().getHistory().get(4).getId());
        assertEquals(epic.getId(), taskManager.getHistoryManager().getHistory().getLast().getId());
        taskManager.getHistoryManager().remove(epic.getId());
        assertEquals(task4.getId(), taskManager.getHistoryManager().getHistory().get(3).getId());
        assertEquals(subtask.getId(), taskManager.getHistoryManager().getHistory().getLast().getId());
    }

    @Test
    void deletedRegularTaskAlsoDeletedInHistory() {
        assertEquals(task.getId(), taskManager.getHistoryManager().getHistory().getFirst().getId());
        taskManager.deleteTask(task.getId());
        assertEquals(task2.getId(), taskManager.getHistoryManager().getHistory().getFirst().getId());
    }

    @Test
    void deletedEpicTaskAlsoDeletedInHistoryWithItsSubtasks() {
        Epic epic = new Epic("Epic1", "Ep Description");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Sub Description", LocalDateTime.of(2021, Month.DECEMBER, 14, 12, 12), Duration.ofHours(1));
        taskManager.createSubtask(subtask, epic.getId());
        taskManager.getEpicById(epic.getId()).orElseThrow();
        taskManager.getSubtaskById(subtask.getId()).orElseThrow();

        Epic epic2 = new Epic("Epic2", "Ep Description");
        taskManager.createEpic(epic2);
        Subtask subtask2 = new Subtask("Subtask2", "Sub Description", LocalDateTime.of(2025, Month.DECEMBER, 11, 12, 12), Duration.ofHours(1));
        taskManager.createSubtask(subtask2, epic2.getId());
        taskManager.getEpicById(epic2.getId()).orElseThrow();
        taskManager.getSubtaskById(subtask2.getId()).orElseThrow();

        assertEquals(8, taskManager.getHistoryManager().getHistory().size());
        assertEquals(epic.getId(), taskManager.getHistoryManager().getHistory().get(4).getId());
        assertEquals(subtask.getId(), taskManager.getHistoryManager().getHistory().get(5).getId());

        taskManager.deleteEpic(epic.getId());

        assertEquals(6, taskManager.getHistoryManager().getHistory().size());
        assertEquals(epic2.getId(), taskManager.getHistoryManager().getHistory().get(4).getId());
        assertEquals(subtask2.getId(), taskManager.getHistoryManager().getHistory().get(5).getId());
    }

    @Test
    void clearAllRegularTasksWorksCorrectInHistory() {
        taskManager.clearTasks();
        assertEquals(0, taskManager.getHistoryManager().getHistory().size());
    }

    @Test
    void clearAllSubtasksWorksCorrectInHistory() {
        Epic epic = new Epic("Epic1", "Ep Description");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Sub Description", LocalDateTime.of(2025, Month.DECEMBER, 11, 12, 12), Duration.ofHours(1));
        taskManager.createSubtask(subtask, epic.getId());
        taskManager.getEpicById(epic.getId()).orElseThrow();
        taskManager.getSubtaskById(subtask.getId()).orElseThrow();

        Epic epic2 = new Epic("Epic2", "Ep Description");
        taskManager.createEpic(epic2);
        Subtask subtask2 = new Subtask("Subtask2", "Sub Description", LocalDateTime.of(2024, Month.DECEMBER, 12, 12, 12), Duration.ofHours(1));
        taskManager.createSubtask(subtask2, epic2.getId());
        taskManager.getEpicById(epic2.getId()).orElseThrow();
        taskManager.getSubtaskById(subtask2.getId()).orElseThrow();

        assertEquals(epic.getId(), taskManager.getHistoryManager().getHistory().get(4).getId());
        assertEquals(subtask.getId(), taskManager.getHistoryManager().getHistory().get(5).getId());
        taskManager.clearSubtasks();
        assertEquals(epic.getId(), taskManager.getHistoryManager().getHistory().get(4).getId());
        assertEquals(epic2.getId(), taskManager.getHistoryManager().getHistory().get(5).getId());
    }

    @Test
    void clearAllEpicWorksCorrectInHistory() {
        Epic epic = new Epic("Epic1", "Ep Description");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Sub Description", LocalDateTime.of(2025, Month.DECEMBER, 21, 12, 12), Duration.ofHours(1));
        taskManager.createSubtask(subtask, epic.getId());
        taskManager.getEpicById(epic.getId()).orElseThrow();
        taskManager.getSubtaskById(subtask.getId()).orElseThrow();

        Epic epic2 = new Epic("Epic2", "Ep Description");
        taskManager.createEpic(epic2);
        Subtask subtask2 = new Subtask("Subtask2", "Sub Description", LocalDateTime.of(2025, Month.DECEMBER, 1, 12, 12), Duration.ofHours(1));
        taskManager.createSubtask(subtask2, epic2.getId());
        taskManager.getEpicById(epic2.getId()).orElseThrow();
        taskManager.getSubtaskById(subtask2.getId()).orElseThrow();

        assertEquals(8, taskManager.getHistoryManager().getHistory().size());
        assertEquals(epic.getId(), taskManager.getHistoryManager().getHistory().get(4).getId());
        assertEquals(subtask.getId(), taskManager.getHistoryManager().getHistory().get(5).getId());
        taskManager.clearEpics();
        assertEquals(4, taskManager.getHistoryManager().getHistory().size());

    }


}
