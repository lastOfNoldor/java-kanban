package main.service;

import main.model.Epic;
import main.model.Subtask;
import main.model.Task;
import main.model.TaskStatus;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final int NUMBER_OF_FIELDS = 8;
    private static final int TASK_ID = 0;
    private static final int CLASS_TYPE = 1;
    private static final int TASK_NAME = 2;
    private static final int TASK_STATUS = 3;
    private static final int TASK_DESCRIPTION = 4;
    private static final int TASK_START_TIME = 5;
    private static final int TASK_DURATION = 6;
    private static final int ADDITIONAL_EPIC_ID_FIELD = 7;
    private final File saveFile;

    public FileBackedTaskManager(File saveFile) {
        this.saveFile = saveFile;
    }

    private void save() {
        try (FileWriter writer = new FileWriter(saveFile)) {
            String example = "id,type,name,status,description,startTime,duration,epic\n";
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
        sb.append(task.getId()).append(",").append(task.getTaskType()).append(",").append(task.getName()).append(",").append(task.getTaskStatus()).append(",").append(task.getDescription()).append(",").append(task.getStartTime()).append(",").append(task.getDuration());
        if (task.getTaskType() == TaskType.SUBTASK) {
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
        if (data.length < NUMBER_OF_FIELDS - 1) {
            return null;
        }
        int id = Integer.parseInt(data[TASK_ID]);
        String name = data[TASK_NAME];
        TaskStatus status = TaskStatus.valueOf(data[TASK_STATUS]);
        String description = data[TASK_DESCRIPTION];
        LocalDateTime startTime = null;
        Duration duration = Duration.ZERO;
        if (!"null".equals(data[TASK_START_TIME])) {
            startTime = parseDataFromString(data[TASK_START_TIME]);
        }
        if (!Duration.ZERO.toString().equals(data[TASK_DURATION])) {
            duration = Duration.parse(data[TASK_DURATION]);
        }
        Task result = null;
        switch (TaskType.valueOf(data[CLASS_TYPE])) {
            case TASK -> result = new Task(name, description, startTime, duration);
            case EPIC -> result = new Epic(name, description, startTime, duration);
            case SUBTASK -> result = new Subtask(name, description, startTime, duration);
        }
        result.setTaskStatus(status);
        result.setId(id);
        if (result.getTaskType() == TaskType.SUBTASK) {
            ((Subtask) result).setEpicId(Integer.parseInt(data[ADDITIONAL_EPIC_ID_FIELD]));
        }
        return result;
    }

    private LocalDateTime parseDataFromString(String data) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return LocalDateTime.parse(data, dateTimeFormatter);
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        int counter = 0;
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
            if (counter < task.getId()) {
                counter = task.getId();
            }
            if (task.getTaskType() == TaskType.SUBTASK) {
                manager.subTasksList.put(task.getId(), (Subtask) task);
                manager.epicTasksList.get(((Subtask) task).getEpicId()).getEpicSubtasks().add(task.getId());
                manager.updateEpicStatus(((Subtask) task).getEpicId());
                manager.updateEpicTime(((Subtask) task).getEpicId());
            } else if (task.getTaskType() == TaskType.EPIC) {
                manager.epicTasksList.put(task.getId(), (Epic) task);
            } else {
                manager.regularTasksList.put(task.getId(), task);
            }
        }
        manager.setIdCounter(counter);
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
