package test;

import static org.junit.jupiter.api.Assertions.*;
import main.model.Epic;
import main.model.Subtask;
import main.model.Task;
import main.model.TaskStatus;
import main.service.InMemoryTaskManager;
import main.service.Managers;
import main.service.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;



class InMemoryTaskManagerTest {
    TaskManager taskManager;

    @BeforeEach
    void init() {
        taskManager = Managers.getDefault();
    }

    @AfterEach
    void clear() {
        InMemoryTaskManager.regularTasksList.clear();
        InMemoryTaskManager.subTasksList.clear();
        InMemoryTaskManager.epicTasksList.clear();
        InMemoryTaskManager.idCounter = 0;
        InMemoryTaskManager.historyManager = Managers.getDefaultHistory();
    }

    @Test
    void tasksEqualsEachOther() {
        Task task = new Task("Test addNewTask", "Test addNewTask description");
        taskManager.createTask(task);
        int id = task.getId();
        Task task2 = taskManager.getTaskById(id);
        assertEquals(task,task2);


    }

    @Test
    void epicEqualsEachOther() {
        Epic epic = new Epic("Test addNewTask", "Test addNewTask description");
        taskManager.createEpic(epic);
        int id = epic.getId();
        Task task2 = taskManager.getEpicById(id);
        assertEquals(epic,task2);

    }

    @Test
    void updateSubTaskStatusUpdatesEpicStatus(){
        Epic et1 = new Epic("Эпик#1", "description");
        Subtask subtask1 = new Subtask("Подзадача первого Эпика", "description", et1.getId());
        taskManager.createEpic(et1);
        taskManager.createSubTask(subtask1);
        assertEquals(TaskStatus.NEW, et1.getTaskStatus());
        subtask1.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask(subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, et1.getTaskStatus());


    }

    @Test
    void done1SubTaskDoesntMakesEpicStatusDoneIfGotMoreSubtasks(){
        Epic et2 = new Epic("Эпик#2", "description");
        Subtask subtask2 = new Subtask("Первая Подзадача второго Эпика", "description", et2.getId());
        Subtask subtask3 = new Subtask("Вторая Подзадача второго Эпика", "description", et2.getId());
        taskManager.createEpic(et2);
        taskManager.createSubTask(subtask2);
        taskManager.createSubTask(subtask3);
        assertEquals(TaskStatus.NEW, et2.getTaskStatus());
        subtask2.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subtask2);
        assertEquals(TaskStatus.IN_PROGRESS, et2.getTaskStatus());
    }


    @Test
    void historyWorks(){
        Task task1 = new Task("Обычное задание#1", "description");
        taskManager.createTask(task1);
        taskManager.getTaskById(1);
        assertEquals(1, ((InMemoryTaskManager) taskManager).historyManager.getHistory().size());

    }

    @Test
    void historyRemove1WhenMoreThan10() {
        Task task1 = new Task("Обычное задание#1", "description");
        Task task2 = new Task("Обычное задание#2", "description");
        Task task3 = new Task("Обычное задание#3", "description");
        Task task4 = new Task("Обычное задание#4", "description");
        Task task5 = new Task("Обычное задание#5", "description");
        Task task6 = new Task("Обычное задание#6", "description");
        Task task7 = new Task("Обычное задание#7", "description");
        Task task8 = new Task("Обычное задание#8", "description");
        Task task9 = new Task("Обычное задание#9", "description");
        Task task10 = new Task("Обычное задание#10", "description");
        Task task11 = new Task("Обычное задание#11", "description");
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);
        taskManager.createTask(task4);
        taskManager.createTask(task5);
        taskManager.createTask(task6);
        taskManager.createTask(task7);
        taskManager.createTask(task8);
        taskManager.createTask(task9);
        taskManager.createTask(task10);
        taskManager.createTask(task11);

        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getTaskById(3);
        taskManager.getTaskById(4);
        taskManager.getTaskById(5);
        taskManager.getTaskById(6);
        taskManager.getTaskById(7);
        taskManager.getTaskById(8);
        taskManager.getTaskById(9);
        taskManager.getTaskById(10);
        List<Task> history = InMemoryTaskManager.historyManager.getHistory();
        assertEquals(1,history.getFirst().getId());
        assertEquals(10,history.getLast().getId());

        taskManager.getTaskById(11);
        List<Task> historyUpdated = InMemoryTaskManager.historyManager.getHistory();

        assertEquals(10, historyUpdated.size());
        assertEquals(2,historyUpdated.getFirst().getId());
        assertEquals(11,historyUpdated.getLast().getId());
    }

}