package main.model;
import java.util.HashSet;

import main.service.InMemoryTaskManager;


public class Epic extends Task {
    private final HashSet<Integer> epicSubtasks;

    public Epic(String name, String description) {
        super(name, description);
        this.epicSubtasks = new HashSet<>();

    }


    public HashSet<Integer> getEpicSubtasks() {
        return epicSubtasks;
    }


    @Override
    public String toString() {
        return "ID: " + getId() +
                ". Название: " + getName() +
                " Описание: " + getDescription() +
                " Кол-во подзадач: " + epicSubtasks.size() +
                ". Status: " + getTaskStatus().toString().charAt(0) +
                getTaskStatus().toString().substring(1).toLowerCase();
    }

    @Override
    public Epic clone() throws CloneNotSupportedException {
        return (Epic) super.clone();
    }
}
