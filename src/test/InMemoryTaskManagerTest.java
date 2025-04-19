package test;

import static org.junit.jupiter.api.Assertions.*;
import main.model.Epic;
import main.model.Subtask;
import main.model.Task;
import main.model.TaskStatus;
import main.service.InMemoryTaskManager;
import main.service.Managers;
import main.service.TaskManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;



class InMemoryTaskManagerTest {
    TaskManager taskManager;

    @BeforeEach
    void init() {
        taskManager = Managers.getDefault();
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
        Subtask subtask1 = new Subtask("Подзадача первого Эпика", "description");
        taskManager.createEpic(et1);
        taskManager.createSubTask(subtask1, et1.getId());
        assertEquals(TaskStatus.NEW, et1.getTaskStatus());
        subtask1.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask(subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, et1.getTaskStatus());
        taskManager.clearSubTasks();
        assertEquals(TaskStatus.NEW, et1.getTaskStatus());
        taskManager.createSubTask(subtask1, et1.getId());
        assertEquals(TaskStatus.IN_PROGRESS, et1.getTaskStatus());
        taskManager.deleteSubTask(subtask1.getId());
        assertEquals(TaskStatus.NEW, et1.getTaskStatus());
    }

    @Test
    void done1SubTaskDoesntMakesEpicStatusDoneIfGotMoreSubtasks(){
        Epic et2 = new Epic("Эпик#2", "description");
        Subtask subtask2 = new Subtask("Первая Подзадача второго Эпика", "description");
        Subtask subtask3 = new Subtask("Вторая Подзадача второго Эпика", "description");
        taskManager.createEpic(et2);
        taskManager.createSubTask(subtask2, et2.getId());
        taskManager.createSubTask(subtask3, et2.getId());
        assertEquals(TaskStatus.NEW, et2.getTaskStatus());
        subtask2.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subtask2);
        assertEquals(TaskStatus.IN_PROGRESS, et2.getTaskStatus());

    }

    @Test
    void historySavesPreviousConditionOfTaskIfItChangesAfter(){
        Epic et2 = new Epic("Эпик#2", "description");
        Subtask subtask2 = new Subtask("Первая Подзадача второго Эпика", "description");
        Subtask subtask3 = new Subtask("Вторая Подзадача второго Эпика", "description");
        taskManager.createEpic(et2);
        taskManager.createSubTask(subtask2, et2.getId());
        taskManager.createSubTask(subtask3, et2.getId());
        assertEquals(TaskStatus.NEW, et2.getTaskStatus());
        taskManager.getEpicById(et2.getId());
        assertEquals(TaskStatus.NEW, taskManager.getHistoryManager().getHistory().getFirst().getTaskStatus());
        subtask2.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subtask2);
        taskManager.getEpicById(et2.getId());
        assertEquals(TaskStatus.NEW, taskManager.getHistoryManager().getHistory().get(0).getTaskStatus());
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getHistoryManager().getHistory().get(1).getTaskStatus());

    }


    @Test
    void historyWorks(){
        Task task1 = new Task("Обычное задание#1", "description");
        taskManager.createTask(task1);
        taskManager.getTaskById(1);
        assertEquals(1, ((InMemoryTaskManager) taskManager).getHistoryManager().getHistory().size());

    }


}