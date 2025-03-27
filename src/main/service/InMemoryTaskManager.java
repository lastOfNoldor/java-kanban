package main.service;

import main.model.Epic;
import main.model.Subtask;
import main.model.Task;

import java.util.HashMap;


public class InMemoryTaskManager implements TaskManager {
    public static HashMap<Integer, Task> regularTasksList = new HashMap<>();
    public static HashMap<Integer, Subtask > subTasksList = new HashMap<>();
    public static HashMap<Integer, Epic> epicTasksList = new HashMap<>();
    public static int idCounter = 0;
    public static HistoryManager historyManager = Managers.getDefaultHistory();


    @Override
    public Task getTaskById(int id) {
        if (regularTasksList.containsKey(id)) {
            historyManager.add(regularTasksList.get(id));
            return regularTasksList.get(id);
        }
        System.out.println("Неверно указан id обычной задачи");
        return null;
    }

    @Override
    public Subtask getSubTaskById(int id) {
        if (subTasksList.containsKey(id)) {
            historyManager.add(subTasksList.get(id));
            return subTasksList.get(id);
        }
        System.out.println("Неверно указан id подзадачи");
        return null;
    }

    @Override
    public Epic getEpicById(int id) {
        if (epicTasksList.containsKey(id)) {
            historyManager.add(subTasksList.get(id));
            return epicTasksList.get(id);
        }
        System.out.println("Неверно указан id Эпика");
        return null;
    }

    @Override
    public void createTask(Task task) {
        regularTasksList.put(task.getId(), task);
    }

    @Override
    public void createSubTask(Subtask subtask) {
        subTasksList.put(subtask.getId(), subtask);
        if (epicTasksList.get(subtask.getEpicId()) instanceof Epic currentEpic) {
            currentEpic.getEpicSubtasks().add(subtask.getId());
        }
    }

    @Override
    public void createEpic(Epic epic) {
        epicTasksList.put(epic.getId(), epic);
    }

    @Override
    public void updateTask(Task task) {
        regularTasksList.put(task.getId(), task);
    }

    @Override
    public void updateSubTask(Subtask subtask) {
        subTasksList.put(subtask.getId(), subtask);
        Epic currentEpic = epicTasksList.get(subtask.getEpicId());
        currentEpic.getEpicSubtasks().add(subtask.getId());
        currentEpic.setTaskStatus();
    }

    @Override
    public void updateEpic(Epic epic) {
        epicTasksList.put(epic.getId(), epic);
    }

    @Override
    public void clearTasks() {
        regularTasksList.clear();
        System.out.println("Все задачи типа Task удалены");
    }

    @Override
    public void clearSubTasks() {
        for (Integer id : subTasksList.keySet()) {
            Subtask currentSubtask = getSubTaskById(id);
            Epic currentEpic = getEpicById(currentSubtask.getEpicId());
            currentEpic.getEpicSubtasks().remove(id);
        }
        for (Integer id : epicTasksList.keySet()) {
            getEpicById(id).setTaskStatus();
        }
        subTasksList.clear();
        System.out.println("Все задачи типа Subtask удалены, статус всех Эпиков автоматически обновлен");
    }

    @Override
    public void clearEpicTasks() {
        subTasksList.clear();
        epicTasksList.clear();
        System.out.println("Все задачи типа Epic удалены (и все SubTask вместе с ними)");
    }


    @Override
    public void deleteTask(int id) {
        if (regularTasksList.containsKey(id)) {
            regularTasksList.remove(id);
        } else {
            System.out.println("Неверно указан id обычной задачи");
        }
    }

    @Override
    public void deleteSubTask(int id) {
        if (subTasksList.containsKey(id)) {
            Subtask currentSubtask = getSubTaskById(id);
            Epic currentEpic = getEpicById(currentSubtask.getEpicId());
            currentEpic.getEpicSubtasks().remove(id);
            subTasksList.remove(id);
            currentEpic.setTaskStatus();
        } else {
            System.out.println("Неверно указан id подзадачи");
        }
    }

    @Override
    public void deleteEpic(int id) {
        if (epicTasksList.containsKey(id)) {
            for (Integer subId : getEpicById(id).getEpicSubtasks()) {
                    subTasksList.remove(subId);
            }
            epicTasksList.remove(id);
        } else {
            System.out.println("Неверно указан id Эпика");
        }
    }

    public void printAllTasks() {
        System.out.println("Задачи:");
        for (Task task : regularTasksList.values()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Epic epic : epicTasksList.values()) {
            System.out.println(epic);

        }
        System.out.println("Подзадачи:");
        for (Subtask subtask : subTasksList.values()) {
            System.out.println(subtask);
        }
        System.out.println("История:");
        for ( Task task : historyManager.getHistory()) {
            System.out.println(task);
        }
    }


}
