package main.model;
import main.service.InMemoryTaskManager;


public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description) {
        super(name, description);
        this.epicId = 0;

    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "ID: " + getId() +
                ". Название: " + getName() +
                " Описание: " + getDescription() +
                " Подзадача Эпика с id: " + getEpicId() +
                ". Status: " + getTaskStatus().toString().charAt(0) +
                getTaskStatus().toString().substring(1).toLowerCase();
    }

    @Override
    public Subtask clone() throws CloneNotSupportedException {
        return (Subtask) super.clone();
    }
}
