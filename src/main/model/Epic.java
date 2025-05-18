package main.model;

import main.service.TaskType;

import java.util.HashSet;


public class Epic extends Task {
    private final HashSet<Integer> epicSubtasks;

    public Epic(String name, String description) {
        super(name, description, null, null);
        this.epicSubtasks = new HashSet<>();

    }

    public Epic(Epic epic) {
        super(epic.getName(), epic.getDescription(), epic.getDuration(), epic.getStartTime());
        setId(epic.getId());
        setTaskStatus(epic.getTaskStatus());
        this.epicSubtasks = epic.getEpicSubtasks();
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.EPIC;
    }

    public HashSet<Integer> getEpicSubtasks() {
        return epicSubtasks;
    }


    @Override
    public String toString() {
        return "ID: " + getId() + ". Название: " + getName() + " Описание: " + getDescription() + " Кол-во подзадач: " + epicSubtasks.size() + ". Status: " + getTaskStatus().toString().charAt(0) + getTaskStatus().toString().substring(1).toLowerCase();
    }


}
