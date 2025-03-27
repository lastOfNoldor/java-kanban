package main.model;
import main.service.InMemoryTaskManager;


public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description, int EpicId) {
        super(name, description);

        this.epicId = EpicId;

    }

    public int getEpicId() {
        return epicId;
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
}
