package main.service;

import main.model.Epic;
import main.model.Subtask;
import main.model.Task;

import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

public interface TaskManager {
    Optional<Task> getTaskById(int id);

    Optional<Subtask> getSubtaskById(int id);

    Optional<Epic> getEpicById(int id);

    void createTask(Task task);

    void createSubtask(Subtask subtask, int epicId);

    void createEpic(Epic epic);

    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    void clearTasks();

    void clearSubtasks();

    void clearEpics();

    void deleteTask(int id);

    void deleteSubtask(int id);

    void deleteEpic(int id);

    void printAllTasks();

    HistoryManager getHistoryManager();


    TreeSet<Task> getPrioritizedTasks();

    List<Task> getRegularTasksList();

    List<Subtask> getSubtasksList();

    List<Epic> getEpicsList();
}
