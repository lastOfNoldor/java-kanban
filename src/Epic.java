import java.util.HashMap;

public class Epic extends Task {
    private HashMap<Integer, Subtask> epicSubtasks;

    public Epic(String name, String description) {
        super(name, description);
        this.epicSubtasks = new HashMap<>();

    }


    public void setTaskStatus() {
        boolean allNew = true;
        boolean allDone = true;
        for (Subtask subtask : epicSubtasks.values()) {
            if (subtask.getTaskStatus() != TaskStatus.NEW) {
                allNew = false;
            }
            if (subtask.getTaskStatus() != TaskStatus.DONE) {
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

    public HashMap<Integer, Subtask> getEpicSubtasks() {
        return epicSubtasks;
    }
}
