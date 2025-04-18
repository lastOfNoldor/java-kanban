package main.service;

import main.model.Epic;
import main.model.Subtask;
import main.model.Task;
import main.model.TaskStatus;

import java.util.HashMap;


public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> regularTasksList = new HashMap<>();
    private final HashMap<Integer, Subtask > subTasksList = new HashMap<>();
    private final HashMap<Integer, Epic> epicTasksList = new HashMap<>();
    private int idCounter = 0;
    private final HistoryManager historyManager = Managers.getDefaultHistory();

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
            historyManager.add(epicTasksList.get(id));
            return epicTasksList.get(id);
        }
        System.out.println("Неверно указан id Эпика");
        return null;
    }

    @Override
    public void createTask(Task task) {
        task.setId(++idCounter);
        regularTasksList.put(task.getId(), task);
    }

    @Override
    public void createSubTask(Subtask subtask, int epicId) {
        if (epicTasksList.get(epicId) instanceof Epic currentEpic) {
            subtask.setId(++idCounter);
            subtask.setEpicId(epicId);
            subTasksList.put(subtask.getId(), subtask);
            currentEpic.getEpicSubtasks().add(subtask.getId());
            updateEpicStatus(subtask.getEpicId());
        }
    }

    @Override
    public void createEpic(Epic epic) {
        epic.setId(++idCounter);
        epicTasksList.put(epic.getId(), epic);
    }

    @Override
    public void updateTask(Task task) {
        if (task.getId() == 0) {
            return;
        }
        regularTasksList.put(task.getId(), task);
    }

    @Override
    public void updateSubTask(Subtask subtask) {
        if (subtask.getId() == 0) {
            return;
        }
        subTasksList.put(subtask.getId(), subtask);
        Epic currentEpic = epicTasksList.get(subtask.getEpicId());
        currentEpic.getEpicSubtasks().add(subtask.getId());
        updateEpicStatus(currentEpic.getId());
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic.getId() == 0) {
            return;
        }
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
            updateEpicStatus(id);
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
            Epic currentEpic = epicTasksList.get(currentSubtask.getEpicId());
            currentEpic.getEpicSubtasks().remove(id);
            subTasksList.remove(id);
            updateEpicStatus(currentEpic.getId());
            updateEpic(currentEpic);
        } else {
            System.out.println("Неверно указан id подзадачи");
        }
    }

    @Override
    public void deleteEpic(int id) {
        if (epicTasksList.containsKey(id)) {
            for (Integer subId : epicTasksList.get(id).getEpicSubtasks()) {
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


    private void updateEpicStatus(int epicId) {
        Epic epic = epicTasksList.get(epicId);
        if (epic.getEpicSubtasks().isEmpty()) {
            epic.setTaskStatus(TaskStatus.NEW);
            return;
        }
        boolean allNew = true;
        boolean allDone = true;
        for (Integer id : epic.getEpicSubtasks()) {
            if (subTasksList.get(id).getTaskStatus() != TaskStatus.NEW) {
                allNew = false;
            }
            if (subTasksList.get(id).getTaskStatus() != TaskStatus.DONE) {
                allDone = false;
            }
        }
        if (allNew) {
            epic.setTaskStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setTaskStatus(TaskStatus.DONE);
        } else {
            epic.setTaskStatus(TaskStatus.IN_PROGRESS);
        }

    }

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }


}
