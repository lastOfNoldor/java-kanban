package main.model;
import main.service.TaskManager;


public class Subtask extends main.model.Task {
    private int epicId;

    public Subtask(String name, String description, int EpicId) {
        super(name, description);
        if (TaskManager.epicTasksList.get(EpicId) instanceof Epic currentEpic) {
            currentEpic.getEpicSubtasks().add(getId());
        }
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
