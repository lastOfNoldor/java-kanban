package main.model;
import main.service.TaskManager;

public class Task {

    private String name;
    private String description;
    private int id;
    private main.model.TaskStatus taskStatus;

    public Task(String name, String description) {
        this.id = TaskManager.idCounter++;
        this.name = name;
        this.description = description;
        taskStatus = main.model.TaskStatus.NEW;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public main.model.TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(main.model.TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    @Override
    public String toString() {
        return "ID: " + getId() +
                ". Название: " + getName() +
                " Описание: " + getDescription() +
                " Status: " + getTaskStatus().toString().charAt(0) +
                getTaskStatus().toString().substring(1).toLowerCase();
    }
}

