package main.model;

import java.util.Objects;

public class Task {

    private final String name;
    private final String description;
    private int id;
    private TaskStatus taskStatus;

    public Task(String name, String description) {
        this.id = 0;
        this.name = name;
        this.description = description;
        taskStatus = TaskStatus.NEW;
    }

    protected Task(Task task) {
        this.id = task.getId();
        this.name = task.getName();
        this.description = task.getDescription();
        this.taskStatus = task.getTaskStatus();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public static Task copy(Task task) {
        if (task instanceof Subtask) {
            return new Subtask((Subtask) task);
        } else if (task instanceof Epic) {
            return new Epic((Epic) task);
        } else {
            return new Task(task);
        }
    }

    @Override
    public String toString() {
        return "ID: " + getId() + ". Название: " + getName() + " Описание: " + getDescription() + " Status: " + getTaskStatus().toString().charAt(0) + getTaskStatus().toString().substring(1).toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(name, task.name) && Objects.equals(description, task.description) && taskStatus == task.taskStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, id, taskStatus);
    }

}

