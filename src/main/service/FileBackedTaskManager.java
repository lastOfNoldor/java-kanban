package main.service;

import main.model.Epic;
import main.model.Subtask;
import main.model.Task;
import main.model.TaskStatus;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File saveFile;

    public FileBackedTaskManager(File saveFile) {
        this.saveFile = saveFile;
    }

    private void save() {
        try (FileWriter writer = new FileWriter(saveFile)) {
            String example = "id,type,name,status,description,epic\n";
            writer.write(example);
            for (Task task : getRegularTasksList()) {
                writer.write(toString(task) + "\n");
            }
            for (Task task : getEpicTasksList()) {
                writer.write(toString(task) + "\n");
            }
            for (Task task : getSubTasksTasksList()) {
                writer.write(toString(task) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения данных.");
        }
    }

    private String toString(Task task) {
        if (task == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",").append(TaskEnum.valueOf(task.getClass().getSimpleName().toUpperCase())).append(",").append(task.getName()).append(",").append(task.getTaskStatus()).append(",").append(task.getDescription());
        if (TaskEnum.valueOf(task.getClass().getSimpleName().toUpperCase()) == TaskEnum.SUBTASK) {
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
        if (TaskEnum.valueOf(result.getClass().getSimpleName().toUpperCase()) == TaskEnum.SUBTASK) {
            ((Subtask) result).setEpicId(Integer.parseInt(data[5]));
        }
        return result;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        List<String> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();   //пропускаю первую строку "id,type,name,status,description,epic";
            while (reader.ready()) {
                data.add(reader.readLine());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String datum : data) {
            Task task = manager.fromString(datum);
            if (task == null) {
                continue;
            }
            if (TaskEnum.valueOf(task.getClass().getSimpleName().toUpperCase()) == TaskEnum.SUBTASK) {
                manager.createSubTask((Subtask) task, ((Subtask) task).getEpicId());
            } else if (TaskEnum.valueOf(task.getClass().getSimpleName().toUpperCase()) == TaskEnum.EPIC) {
                manager.createEpic((Epic) task);
            } else {
                manager.createTask(task);
            }
        }
        return manager;
    }


    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createSubTask(Subtask subtask, int epicId) {
        super.createSubTask(subtask, epicId);
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubTask(Subtask subtask) {
        super.updateSubTask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearSubTasks() {
        super.clearSubTasks();
        save();
    }

    @Override
    public void clearEpicTasks() {
        super.clearEpicTasks();
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteSubTask(int id) {
        super.deleteSubTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }
}
