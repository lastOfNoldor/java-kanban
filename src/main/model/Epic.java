package main.model;

import main.service.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;


public class Epic extends Task {
    private final HashSet<Integer> epicSubtasks;
    private LocalDateTime endTime;

    public Epic() {
        super();
        if (super.getDuration() == null) {
            super.setDuration(Duration.ZERO);
        }
        this.epicSubtasks = new HashSet<>();
    }

    public Epic(String name, String description) {
        super(name, description, null, Duration.ZERO);
        this.epicSubtasks = new HashSet<>();

    }

    public Epic(Epic epic) {
        super(epic.getName(), epic.getDescription(), epic.getStartTime(), epic.getDuration());
        setId(epic.getId());
        setTaskStatus(epic.getTaskStatus());
        this.endTime = epic.getEndTime();
        this.epicSubtasks = epic.getEpicSubtasks();
    }

    public Epic(String name, String description, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
        this.epicSubtasks = new HashSet<>();
    }

    @Override
    public void validate() {
        if (super.getName() == null || super.getName().isBlank()) {
            throw new IllegalArgumentException("Имя не указано!");
        }
        if (super.getDescription() == null || super.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание не указано!");
        }
        if (super.getTaskStatus() == null) {
            throw new IllegalArgumentException("Статус задачи не указан!");
        }
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.EPIC;
    }

    public HashSet<Integer> getEpicSubtasks() {
        return epicSubtasks;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "ID: " + getId() + ". Название: " + getName() + " Описание: " + getDescription() + " Кол-во подзадач: " + epicSubtasks.size() + ". Время старта: " + getStartTime() + " Продолжительность: " + getDuration() + " Время завершения: " + getEndTime() + " Статус: " + getTaskStatus().toString().charAt(0) + getTaskStatus().toString().substring(1).toLowerCase();
    }


}
