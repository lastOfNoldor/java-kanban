package main.service;

import main.model.Epic;
import main.model.Subtask;
import main.model.Task;
import main.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> regularTasksList = new HashMap<>();
    protected final Map<Integer, Subtask> subTasksList = new HashMap<>();
    protected final Map<Integer, Epic> epicTasksList = new HashMap<>();
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())));
    private int idCounter = 0;
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private void timeCrossCheck(Task task) {
        if (isTaskTimeCrossed(task)) {
            throw new IllegalTaskTimeException("Время выполнения задачи " + task.getName() + " пересекается по времени с другой задачей в списке!");
        }
    }

    public TreeSet<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }


    private boolean isTaskTimeCrossed(Task task1) {
        boolean hasIntersection = prioritizedTasks.stream().anyMatch(task2 -> isTaskInSameTimeWithAnother(task1, task2));
        prioritizedTasks.removeIf(task2 -> task2.getId() == task1.getId());
        return hasIntersection;

    }

    private boolean isTaskInSameTimeWithAnother(Task task1, Task task2) {
        if (task1.getId() == task2.getId()) {
            return false;
        }
        if (task1.getStartTime() == null || task2.getStartTime() == null) {
            return false;
        }
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();
        return !end1.isBefore(start2) && !end2.isBefore(start1);

    }

    @Override
    public Optional<Task> getTaskById(int id) {
        if (regularTasksList.containsKey(id)) {
            historyManager.add(new Task(regularTasksList.get(id)));
            return Optional.of(new Task(regularTasksList.get(id)));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Subtask> getSubTaskById(int id) {
        if (subTasksList.containsKey(id)) {
            historyManager.add(new Subtask(subTasksList.get(id)));
            return Optional.of(new Subtask(subTasksList.get(id)));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Epic> getEpicById(int id) {
        if (epicTasksList.containsKey(id)) {
            historyManager.add(new Epic(epicTasksList.get(id)));
            return Optional.of(new Epic(epicTasksList.get(id)));
        }
        return Optional.empty();
    }

    @Override
    public void createTask(Task task) {
        if (task == null) {
            return;
        }
        timeCrossCheck(task);
        task.setId(++idCounter);
        regularTasksList.put(task.getId(), new Task(task));
        prioritizedCheck(task);
    }

    @Override
    public void createSubTask(Subtask subtask, int epicId) {
        if (!epicTasksList.containsKey(epicId) || subtask == null) {
            return;
        }
        subtask.setEpicId(epicId);
        timeCrossCheck(subtask);
        subtask.setId(++idCounter);
        subTasksList.put(subtask.getId(), new Subtask(subtask));
        prioritizedCheck(subtask);
        Epic currentEpic = epicTasksList.get(epicId);
        currentEpic.getEpicSubtasks().add(subtask.getId());
        updateEpic(currentEpic);
    }

    @Override
    public void createEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        epic.setId(++idCounter);
        epicTasksList.put(epic.getId(), new Epic(epic));
    }

    @Override
    public void updateTask(Task task) {
        if (task.getId() == 0) {
            return;
        }
        timeCrossCheck(task);
        regularTasksList.put(task.getId(), task);
        prioritizedCheck(task);
    }

    @Override
    public void updateSubTask(Subtask subtask) {
        if (subtask.getId() == 0) {
            return;
        }
        timeCrossCheck(subtask);
        subTasksList.put(subtask.getId(), subtask);
        Epic currentEpic = epicTasksList.get(subtask.getEpicId());
        currentEpic.getEpicSubtasks().add(subtask.getId());
        prioritizedCheck(subtask);
        updateEpic(currentEpic);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic.getId() == 0) {
            return;
        }
        updateEpicStatus(epic);
        updateEpicTime(epic);
        epicTasksList.put(epic.getId(), epic);
    }

    @Override
    public void clearTasks() {
        clearAllTasksInManager();
        regularTasksList.clear();
        System.out.println("Все задачи типа Task удалены");
    }

    private void clearAllTasksInManager() {
        for (Integer id : regularTasksList.keySet()) {
            historyManager.remove(id);
            prioritizedTasks.remove(regularTasksList.get(id));
        }
    }

    @Override
    public void clearSubTasks() {
        clearAllSubtasksInEpics();
        clearAllSubtasksInManager();
        subTasksList.clear();
        for (Epic epic : epicTasksList.values()) {
            updateEpic(epic);
        }
        System.out.println("Все задачи типа Subtask удалены, статус всех Эпиков автоматически обновлен");
    }

    private void clearAllSubtasksInEpics() {
        subTasksList.values().forEach(subtask -> {
            Epic epic = epicTasksList.get(subtask.getEpicId());
            epic.getEpicSubtasks().remove(subtask.getId());
        });
    }

    @Override
    public void clearEpicTasks() {
        clearAllSubtasksInManager();
        clearEpicsItself();
        subTasksList.clear();
        epicTasksList.clear();
        System.out.println("Все задачи типа Epic удалены (и все SubTask вместе с ними)");
    }

    private void clearAllSubtasksInManager() {
        for (Integer id : subTasksList.keySet()) {
            historyManager.remove(id);
            prioritizedTasks.remove(subTasksList.get(id));
        }
    }

    private void clearEpicsItself() {
        for (Integer id : epicTasksList.keySet()) {
            historyManager.remove(id);
            prioritizedTasks.remove(epicTasksList.get(id));
        }
    }

    @Override
    public void deleteTask(int id) {
        if (regularTasksList.containsKey(id)) {
            historyManager.remove(id);
            deleteTaskFromPrioritized(regularTasksList.get(id));
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
            deleteTaskFromPrioritized(currentSubtask);
            Epic currentEpic = epicTasksList.get(currentSubtask.getEpicId());
            currentEpic.getEpicSubtasks().remove(id);
            subTasksList.remove(id);
            updateEpic(currentEpic);

        } else {
            System.out.println("Неверно указан id подзадачи");
        }
    }

    @Override
    public void deleteEpic(int id) {
        if (epicTasksList.containsKey(id)) {
            deleteAllSubtasksOfSingleEpic(id);
            historyManager.remove(id);
            deleteTaskFromPrioritized(epicTasksList.get(id));
            epicTasksList.remove(id);
        } else {
            System.out.println("Неверно указан id Эпика");
        }
    }

    private void deleteAllSubtasksOfSingleEpic(int id) {
        for (Integer subId : epicTasksList.get(id).getEpicSubtasks()) {
            historyManager.remove(subId);
            deleteTaskFromPrioritized(subTasksList.get(subId));
            subTasksList.remove(subId);
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

    protected void updateEpicStatus(Epic epic) {
        List<TaskStatus> statuses = getEpicSubtaskStatuses(epic);
        if (isAllNew(statuses) || statuses.isEmpty()) {
            epic.setTaskStatus(TaskStatus.NEW);
        } else if (isAllDone(statuses)) {
            epic.setTaskStatus(TaskStatus.DONE);
        } else {
            epic.setTaskStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private boolean isAllNew(List<TaskStatus> statuses) {
        return statuses.stream().allMatch(status -> status == TaskStatus.NEW);
    }

    private boolean isAllDone(List<TaskStatus> statuses) {
        return statuses.stream().allMatch(status -> status == TaskStatus.DONE);
    }

    private List<TaskStatus> getEpicSubtaskStatuses(Epic epic) {
        return epic.getEpicSubtasks().stream().map(subTasksList::get).filter(Objects::nonNull).map(Task::getTaskStatus).toList();
    }

    protected void updateEpicTime(Epic epic) {
        epic.setStartTime(epic.getEpicSubtasks().stream().map(subTasksList::get).filter(Objects::nonNull).map(Task::getStartTime).filter(Objects::nonNull).min(LocalDateTime::compareTo).orElse(null));
        epic.setDuration(epic.getEpicSubtasks().stream().map(subTasksList::get).filter(Objects::nonNull).map(Task::getDuration).filter(Objects::nonNull).reduce(Duration.ZERO, Duration::plus));
        epic.setEndTime(epic.getEpicSubtasks().stream().map(subTasksList::get).filter(Objects::nonNull).map(Task::getEndTime).filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null));


    }

    private void prioritizedCheck(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(new Task(task));
        }
    }

    private void deleteTaskFromPrioritized(Task task) {
        prioritizedTasks.remove(task);
    }

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public List<Task> getRegularTasksList() {

        return new ArrayList<>(regularTasksList.values());
    }

    public List<Epic> getEpicTasksList() {
        return new ArrayList<>(epicTasksList.values());
    }

    public List<Subtask> getSubTasksTasksList() {
        return new ArrayList<>(subTasksList.values());
    }

    protected void setIdCounter(int idCounter) {
        this.idCounter = idCounter;
    }

}
