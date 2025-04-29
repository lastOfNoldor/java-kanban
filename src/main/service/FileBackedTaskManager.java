package main.service;

import main.model.Epic;
import main.model.Subtask;
import main.model.Task;
import main.model.TaskStatus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File saveFile;

    public FileBackedTaskManager(File saveFile) {
        this.saveFile = saveFile;
    }

    private void save() {
        try (FileWriter writer = new FileWriter(saveFile)) {
            String example = "id,type,name,status,description,epic";
            writer.write(example);
            for (Task task : getRegularTasksList().values()) {
                writer.write(toString(task));
            }
            for (Task task : getEpicTasksList().values()) {
                writer.write(toString(task));
            }
            for (Task task : getSubTasksTasksList().values()) {
                writer.write(toString(task));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String toString(Task task) {
        if (task == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",").append(TaskEnum.valueOf(task.getClass().toString())).append(",").append(task.getName()).append(",").append(task.getTaskStatus()).append(",").append(task.getDescription());
        if (task instanceof Subtask) {
            sb.append(",");
            sb.append(((Subtask) task).getEpicId());
        }
        return sb.toString();
    }

    private Task fromString(String value) {
        if (value == null) {
            return null;
        }
        String[] data = value.split(",");
        if (data.length < 5) {
            return null;
        }
        int id = Integer.parseInt(data[0]);
        String name = data[2];
        TaskStatus status = TaskStatus.valueOf(data[3]);
        String description = data[4];
        Task result = null;
        switch (TaskEnum.valueOf(data[1])) {
            case TASK -> result = new Task(name, description);
            case EPIC -> result = new Epic(name, description);
            case SUBTASK -> result = new Subtask(name, description);
        }
        result.setTaskStatus(status);
        result.setId(id);
        if (result instanceof Subtask) {
            ((Subtask) result).setEpicId(Integer.parseInt(data[5]));
        }
        return result;
    }
}
