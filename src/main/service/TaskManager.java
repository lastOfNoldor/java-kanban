package main.service;

import main.model.Epic;
import main.model.Subtask;
import main.model.Task;

import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

public interface TaskManager {
    Optional<Task> getTaskById(int id);

    Optional<Subtask> getSubTaskById(int id);

    Optional<Epic> getEpicById(int id);

    void createTask(Task task);

    void createSubTask(Subtask subtask, int epicId);

    void createEpic(Epic epic);

    void updateTask(Task task);

    void updateSubTask(Subtask subtask);

    void clearTasks();

    void clearSubTasks();

    void clearEpicTasks();

    void deleteTask(int id);

    void deleteSubTask(int id);

    void deleteEpic(int id);

    void printAllTasks();

    HistoryManager getHistoryManager();


    TreeSet<Task> getPrioritizedTasks();

    List<Task> getRegularTasksList();

    List<Subtask> getSubTasksTasksList();

    List<Epic> getEpicTasksList();
}
