package main.model;

import main.service.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

public class Task {

    private final String name;
    private final String description;
    private int id;
    private TaskStatus taskStatus;
    private Duration duration;
    private LocalDateTime startTime;


    public Task(String name, String description, LocalDateTime startTime, Duration duration) {
        this.id = 0;
        this.name = name;
        this.description = description;
        taskStatus = TaskStatus.NEW;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(Task task) {
        this.id = task.getId();
        this.name = task.getName();
        this.description = task.getDescription();
        this.taskStatus = task.getTaskStatus();
        this.startTime = task.startTime;
        this.duration = task.duration;
    }

    public LocalDateTime getEndTime() {
        if (duration == Duration.ZERO || startTime == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public TaskType getTaskType() {
        return TaskType.TASK;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id == 0) {
            return;
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    @Override
    public String toString() {
        return "ID: " + getId() + ". Название: " + getName() + " Описание: " + getDescription() + " Время старта: " + getStartTime() + " Продолжительность: " + getDuration() + " Время завершения: " + getEndTime() + " Статус: " + getTaskStatus().toString().charAt(0) + getTaskStatus().toString().substring(1).toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
}

