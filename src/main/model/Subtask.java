package main.model;


import main.service.TaskType;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description) {
        super(name, description);
        this.epicId = 0;

    }

    public Subtask(Subtask subtask) {
        super(subtask.getName(), subtask.getDescription());
        setId(subtask.getId());
        setEpicId(subtask.getEpicId());
        setTaskStatus(subtask.getTaskStatus());
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "ID: " + getId() + ". Название: " + getName() + " Описание: " + getDescription() + " Подзадача Эпика с id: " + getEpicId() + ". Status: " + getTaskStatus().toString().charAt(0) + getTaskStatus().toString().substring(1).toLowerCase();
    }

}
