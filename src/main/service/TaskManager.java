package main.service;

import main.model.Epic;
import main.model.Subtask;
import main.model.Task;
import java.util.HashMap;


public class TaskManager {
    public static HashMap<Integer, Task> regularTasksList = new HashMap<>();
    public static HashMap<Integer, Subtask > subTasksList = new HashMap<>();
    public static HashMap<Integer, Epic> epicTasksList = new HashMap<>();
    public static int idCounter = 0;

    public Task getTaskById(int id) {
        if (regularTasksList.containsKey(id)) {
            return regularTasksList.get(id);
        }
        System.out.println("Неверно указан id обычной задачи");
        return null;
    }

    public Subtask getSubTaskById(int id) {
        if (subTasksList.containsKey(id)) {
            return subTasksList.get(id);
        }
        System.out.println("Неверно указан id подзадачи");
        return null;
    }

    public Epic getEpicById(int id) {
        if (epicTasksList.containsKey(id)) {
            return epicTasksList.get(id);
        }
        System.out.println("Неверно указан id Эпика");
        return null;
    }

    public void createTask(Task task) {
        regularTasksList.put(task.getId(), task);
    }

    public void createSubTask(Subtask subtask) {
        subTasksList.put(subtask.getId(), subtask);
    }

    public void createEpic(Epic epic) {
        epicTasksList.put(epic.getId(), epic);
    }

    public void updateTask(Task task) {
        regularTasksList.put(task.getId(), task);
    }

    public void updateSubTask(Subtask subtask) {
        subTasksList.put(subtask.getId(), subtask);
        Epic currentEpic = epicTasksList.get(subtask.getEpicId());
        currentEpic.getEpicSubtasks().add(subtask.getId());
        currentEpic.setTaskStatus();
    }

    public void updateEpic(Epic epic) {
        epicTasksList.put(epic.getId(), epic);
    }

    public void clearTasks() {
        regularTasksList.clear();
        System.out.println("Все задачи типа Task удалены");
    }

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

    public void clearEpicTasks() {
        subTasksList.clear();
        epicTasksList.clear();
        System.out.println("Все задачи типа Epic удалены (и все SubTask вместе с ними)");
    }


    public void deleteTask(int id) {
        if (regularTasksList.containsKey(id)) {
            regularTasksList.remove(id);
        } else {
            System.out.println("Неверно указан id обычной задачи");
        }
    }

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



}
