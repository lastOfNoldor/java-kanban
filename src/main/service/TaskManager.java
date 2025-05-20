package main.service;

import main.model.Epic;
import main.model.Subtask;
import main.model.Task;

import java.util.TreeSet;

public interface TaskManager {
    Task getTaskById(int id);

    Subtask getSubTaskById(int id);

    Epic getEpicById(int id);

    void createTask(Task task);

    void createSubTask(Subtask subtask, int epicId);

    void createEpic(Epic epic);

    void updateTask(Task task);

    void updateSubTask(Subtask subtask);

    void updateEpic(Epic epic);

    void clearTasks();

    void clearSubTasks();

    void clearEpicTasks();

    void deleteTask(int id);

    void deleteSubTask(int id);

    void deleteEpic(int id);

    void printAllTasks();

    HistoryManager getHistoryManager();


    TreeSet<Task> getPrioritizedTasks();
}
