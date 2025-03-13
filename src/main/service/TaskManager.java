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
        if (subTasksList.containsKey(id)) {
            return subTasksList.get(id);
        }
        if (epicTasksList.containsKey(id)) {
            return epicTasksList.get(id);
        }
        System.out.println("Неверно указан id задачи");
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
        for (Integer id : regularTasksList.keySet()) {
            deleteTask(id);
        }
        System.out.println("Все задачи типа main.Task удалены");
    }

    public void clearSubTasks() {
        for (Integer id : subTasksList.keySet()) {
            deleteTask(id);
        }
        System.out.println("Все задачи типа main.model.Subtask удалены");
    }

    public void clearEpicTasks() {
        for (Integer id : epicTasksList.keySet()) {
            deleteTask(id);
        }
        System.out.println("Все задачи типа main.Epic удалены");
    }

    //логика удаления по id для любого объекта без проблем умещается в один метод
    public void deleteTask(int id) {
        if (regularTasksList.containsKey(id)) {
            regularTasksList.remove(id);
            return;
        }
        if (subTasksList.containsKey(id)) {
            Subtask currentSubtask = subTasksList.get(id);
            Epic currentEpic = epicTasksList.get(currentSubtask.getEpicId());
            currentEpic.getEpicSubtasks().remove(id);
            subTasksList.remove(id);
            return;
        }
        if (epicTasksList.containsKey(id)) {
            for (Integer subId : epicTasksList.get(id).getEpicSubtasks()) {
                    subTasksList.remove(subId);
            }
            return;
        }
        System.out.println("Неверно указан id задачи");
    }



}
