package main.model;
import java.util.HashSet;

import main.service.InMemoryTaskManager;


public class Epic extends Task {
    protected HashSet<Integer> epicSubtasks;

    public Epic(String name, String description) {
        super(name, description);
        this.epicSubtasks = new HashSet<>();

    }


    public void setTaskStatus() {
        if (epicSubtasks.isEmpty()) {
            super.setTaskStatus(TaskStatus.NEW);
            return;
        }
        boolean allNew = true;
        boolean allDone = true;
        for (Integer id : epicSubtasks) {
            if (InMemoryTaskManager.subTasksList.get(id).getTaskStatus() != TaskStatus.NEW) {
                allNew = false;
            }
            if (InMemoryTaskManager.subTasksList.get(id).getTaskStatus() != TaskStatus.DONE) {
                allDone = false;
            }
        }
        if (allNew) {
            super.setTaskStatus(TaskStatus.NEW);
        } else if (allDone) {
            super.setTaskStatus(TaskStatus.DONE);
        } else {
            super.setTaskStatus(TaskStatus.IN_PROGRESS);
        }
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
}
