package main.service;

import main.model.Epic;
import main.model.Subtask;
import main.model.Task;
import main.model.TaskStatus;

import java.util.HashMap;
import java.util.Map;


public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> regularTasksList = new HashMap<>();
    private final Map<Integer, Subtask> subTasksList = new HashMap<>();
    private final Map<Integer, Epic> epicTasksList = new HashMap<>();
    private int idCounter = 0;
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public Task getTaskById(int id) {
        if (regularTasksList.containsKey(id)) {
            historyManager.add(new Task(regularTasksList.get(id)));
            return new Task(regularTasksList.get(id));
        }
        System.out.println("Неверно указан id обычной задачи");
        return null;
    }

    @Override
    public Subtask getSubTaskById(int id) {
        if (subTasksList.containsKey(id)) {
            historyManager.add(new Subtask(subTasksList.get(id)));
            return new Subtask(subTasksList.get(id));
        }
        System.out.println("Неверно указан id подзадачи");
        return null;
    }

    @Override
    public Epic getEpicById(int id) {
        if (epicTasksList.containsKey(id)) {
            historyManager.add(new Epic(epicTasksList.get(id)));
            return new Epic(epicTasksList.get(id));
        }
        System.out.println("Неверно указан id Эпика");
        return null;
    }

    @Override
    public void createTask(Task task) {
        task.setId(++idCounter);
        regularTasksList.put(task.getId(), new Task(task));
    }

    @Override
    public void createSubTask(Subtask subtask, int epicId) {
        if (epicTasksList.get(epicId) instanceof Epic currentEpic) {
            subtask.setId(++idCounter);
            subtask.setEpicId(epicId);
            subTasksList.put(subtask.getId(), new Subtask(subtask));
            currentEpic.getEpicSubtasks().add(subtask.getId());
            updateEpicStatus(subtask.getEpicId());
        }
    }

    @Override
    public void createEpic(Epic epic) {
        epic.setId(++idCounter);
        epicTasksList.put(epic.getId(), new Epic(epic));
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
        for (Integer id : regularTasksList.keySet()) {
            historyManager.remove(id);
        }
        regularTasksList.clear();
        System.out.println("Все задачи типа Task удалены");
    }

    @Override
    public void clearSubTasks() {
        for (Subtask currentSubtask : subTasksList.values()) {
            Epic currentEpic = epicTasksList.get(currentSubtask.getEpicId());
            currentEpic.getEpicSubtasks().remove(currentSubtask.getId());
        }
        for (Integer id : subTasksList.keySet()) {
            historyManager.remove(id);
        }
        subTasksList.clear();
        for (Integer id : epicTasksList.keySet()) {
            updateEpicStatus(id);
        }
        System.out.println("Все задачи типа Subtask удалены, статус всех Эпиков автоматически обновлен");
    }

    @Override
    public void clearEpicTasks() {
        for (Integer id : subTasksList.keySet()) {
            historyManager.remove(id);
        }
        for (Integer id : epicTasksList.keySet()) {
            historyManager.remove(id);
        }
        subTasksList.clear();
        epicTasksList.clear();
        System.out.println("Все задачи типа Epic удалены (и все SubTask вместе с ними)");
    }


    @Override
    public void deleteTask(int id) {
        if (regularTasksList.containsKey(id)) {
            historyManager.remove(id);
            regularTasksList.remove(id);
        } else {
            System.out.println("Неверно указан id обычной задачи");
        }
    }

    @Override
    public void deleteSubTask(int id) {
        if (subTasksList.containsKey(id)) {
            historyManager.remove(id);
            Subtask currentSubtask = subTasksList.get(id);
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
                historyManager.remove(subId);
                subTasksList.remove(subId);
            }
            historyManager.remove(id);
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
        for (Task task : historyManager.getHistory()) {
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

    public Map<Integer, Task> getRegularTasksList() {
        return regularTasksList;
    }

    public Map<Integer, Epic> getEpicTasksList() {
        return epicTasksList;
    }

    public Map<Integer, Subtask> getSubTasksTasksList() {
        return subTasksList;
    }

}
