package main.test;

import main.model.Epic;
import main.model.Subtask;
import main.model.Task;
import main.model.TaskStatus;
import main.service.IllegalTaskTimeException;
import main.service.TaskManager;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected TaskManager taskManager;
    protected Task task;
    protected Epic epic1;
    protected Subtask subtask1;
    protected Epic epic2;
    protected Subtask subtask2;
    protected Subtask subtask3;


    @Test
    void tasksEqualsEachOther() {
        int id = task.getId();
        Task task2 = taskManager.getTaskById(id).get();
        assertEquals(task, task2);
    }

    @Test
    void epicEqualsEachOther() {
        int id = epic1.getId();
        Epic epicAnother = taskManager.getEpicById(id).get();
        assertEquals(epic1, epicAnother);
    }

    @Test
    void updateSubTaskStatusUpdatesEpicStatus() {
        assertEquals(TaskStatus.NEW, epic1.getTaskStatus());
        subtask1.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask(subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epic1.getId()).get().getTaskStatus());
        taskManager.clearSubTasks();
        assertEquals(TaskStatus.NEW, taskManager.getEpicById(epic1.getId()).get().getTaskStatus());
        taskManager.createSubTask(subtask1, epic1.getId());
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epic1.getId()).get().getTaskStatus());
        taskManager.deleteSubTask(subtask1.getId());
        assertEquals(TaskStatus.NEW, taskManager.getEpicById(epic1.getId()).get().getTaskStatus());
    }

    @Test
    void done1SubTaskDoesNotMakesEpicStatusDoneIfGotMoreSubtasks() {

        assertEquals(TaskStatus.NEW, epic2.getTaskStatus());
        subtask2.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subtask2);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epic2.getId()).get().getTaskStatus());

    }

    @Test
    void historyWorks() {
        taskManager.getTaskById(task.getId()).get();
        assertEquals(1, taskManager.getHistoryManager().getHistory().size());

    }

    @Test
    void wrapProtectsFromSetIdAndTaskStatus() {
        taskManager.getTaskById(task.getId()).get().setId(1111);
        taskManager.updateTask(task);
        assertEquals(1, task.getId());
        taskManager.getTaskById(task.getId()).get().setTaskStatus(TaskStatus.DONE);
        taskManager.updateTask(task);
        assertEquals(TaskStatus.NEW, taskManager.getTaskById(task.getId()).get().getTaskStatus());

    }

    @Test
    void prioritizedListWorksCorrect() {
        taskManager.getPrioritizedTasks().forEach(System.out::println);
        System.out.println(taskManager.getPrioritizedTasks().size());
        Optional<LocalDateTime> startTime = taskManager.getPrioritizedTasks().stream().map(Task::getStartTime).filter(Objects::nonNull).reduce((prev, current) -> {
            if (prev.isAfter(current)) {
                throw new RuntimeException();
            }
            return current;
        });
        assertFalse(startTime.isEmpty());
    }

    @Test
    void prioritizedListUpdatesProperly() {
        task.setStartTime(LocalDateTime.of(1984, Month.DECEMBER, 1, 1, 1));
        taskManager.updateTask(task);
        LocalDateTime startTime1 = taskManager.getPrioritizedTasks().getFirst().getStartTime();
        task.setStartTime(LocalDateTime.of(2077, Month.DECEMBER, 1, 1, 1));
        taskManager.updateTask(task);
        LocalDateTime startTime2 = taskManager.getPrioritizedTasks().getFirst().getStartTime();
        assertNotEquals(startTime1, startTime2);
        assertEquals(task.getStartTime(), taskManager.getPrioritizedTasks().getLast().getStartTime());
        Task taskForRemove = taskManager.getPrioritizedTasks().getLast();
        taskManager.deleteTask(taskForRemove.getId());
        Task newLastTask = taskManager.getPrioritizedTasks().getLast();
        assertNotEquals(taskForRemove, newLastTask);
        int id = taskManager.getPrioritizedTasks().getFirst().getId();
        Subtask subtaskForUpdate = taskManager.getSubTaskById(id).get();
        subtaskForUpdate.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subtaskForUpdate);
        assertEquals(TaskStatus.DONE, taskManager.getPrioritizedTasks().getFirst().getTaskStatus());
    }

    @Test
    void timeWorksProperlyInEpic() {
        Epic epic3 = new Epic("Epic3", "test");
        taskManager.createEpic(epic3);
        assertNull(epic3.getStartTime());
        assertEquals(epic3.getDuration(), Duration.ZERO);
        assertNull(epic3.getEndTime());
        Subtask subtask4 = new Subtask("Subtask4", "test", LocalDateTime.of(2000, Month.DECEMBER, 1, 1, 1), Duration.ofHours(1));
        taskManager.createSubTask(subtask4, epic3.getId());
        epic3 = taskManager.getEpicById(epic3.getId()).get();
        assertEquals(subtask4.getStartTime(), epic3.getStartTime());
        assertEquals(subtask4.getDuration(), epic3.getDuration());
        assertEquals(subtask4.getEndTime(), epic3.getEndTime());
        subtask4.setStartTime(LocalDateTime.of(2001, Month.NOVEMBER, 1, 1, 1));
        subtask4.setDuration(Duration.ofHours(10));
        taskManager.updateSubTask(subtask4);
        epic3 = taskManager.getEpicById(epic3.getId()).get();
        assertEquals(epic3.getStartTime(), LocalDateTime.of(2001, Month.NOVEMBER, 1, 1, 1));
        assertEquals(epic3.getDuration(), Duration.ofHours(10));
        assertEquals(epic3.getEndTime(), subtask4.getEndTime());
        Subtask subtask5 = new Subtask("Subtask4", "test", LocalDateTime.of(2001, Month.DECEMBER, 1, 1, 1), Duration.ofHours(1));
        taskManager.createSubTask(subtask5, epic3.getId());
        epic3 = taskManager.getEpicById(epic3.getId()).get();
        assertEquals(epic3.getStartTime(), subtask4.getStartTime());
        Duration epicDuration = subtask4.getDuration().plus(subtask5.getDuration());
        assertEquals(epicDuration, epic3.getDuration());
        assertEquals(subtask5.getEndTime(), taskManager.getEpicById(epic3.getId()).get().getEndTime());
        Subtask subtask6 = new Subtask("subtask6", "test", LocalDateTime.of(2002, Month.DECEMBER, 1, 1, 1), Duration.ofDays(1));
        taskManager.createSubTask(subtask6, epic3.getId());
        taskManager.deleteSubTask(subtask5.getId());
        epic3 = taskManager.getEpicById(epic3.getId()).get();
        assertEquals(epic3.getStartTime(), LocalDateTime.of(2001, Month.NOVEMBER, 1, 1, 1));
        assertEquals(epic3.getDuration(), Duration.ofHours(34));
        assertEquals(epic3.getEndTime(), subtask6.getEndTime());
    }

    @Test
    void exceptionIfTimeCrosses() {
        task.setStartTime(LocalDateTime.of(1984, Month.DECEMBER, 1, 1, 1));
        taskManager.updateTask(task);
        subtask2.setStartTime(LocalDateTime.of(1984, Month.DECEMBER, 1, 1, 1));
        assertThrows(IllegalTaskTimeException.class, () -> taskManager.updateSubTask(subtask2));
    }

    @Test
    void exceptionIfTimeCrossesByDuration() {
        task.setDuration(Duration.ofDays(11111));
        assertThrows(IllegalTaskTimeException.class, () -> taskManager.updateTask(task));
    }
}
