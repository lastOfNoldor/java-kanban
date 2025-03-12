public class Subtask extends Task {
    private int EpicId;

    public Subtask(String name, String description, int EpicId) {
        super(name, description);
        if (TaskManager.tasksList.get(EpicId) instanceof Epic currentEpic) {
            currentEpic.getEpicSubtasks().put(getId(), this);
        }
        this.EpicId = EpicId;

    }

    public int getEpicId() {
        return EpicId;
    }
}
