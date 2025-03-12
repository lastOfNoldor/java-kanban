import java.util.HashMap;


public class TaskManager {
    public static HashMap<Integer, Task> tasksList = new HashMap<>();
    public static int idCounter = 0;

    public void  printTasks() {
        for (Task task : tasksList.values()) {
            System.out.println(task);
        }
        System.out.println();
        System.out.println("-".repeat(11));
        System.out.println();
    }

    public void  clearTasks() {
        tasksList.clear();
        System.out.println("Все задачи удалены");
    }

    public Task getTaskById(int id) {
        return tasksList.get(id);
    }

    public void createTask(Task task) {
            tasksList.put(task.getId(), task);
    }

    public void updateTask(Task task) {
            tasksList.put(task.getId(), task);
            if (task instanceof Subtask) {
                updateEpic((Subtask) task);
            }
    }

    private void updateEpic(Subtask subtask) {
        Epic currentEpic = (Epic) tasksList.get(subtask.getEpicId());
        currentEpic.getEpicSubtasks().put(subtask.getId(), subtask);
        currentEpic.setTaskStatus();
    }

    public void deleteTask(int id) {
        if (tasksList.containsKey(id)) {
            if (tasksList.get(id) instanceof Subtask currentSubtask) {
                Epic currentEpic = (Epic) tasksList.get(currentSubtask.getEpicId());
                currentEpic.getEpicSubtasks().remove(currentSubtask.getId());
            }
            if (tasksList.get(id) instanceof Epic currentEpic) {
                for (Subtask subtask : currentEpic.getEpicSubtasks().values()) {
                    tasksList.remove(subtask.getId());
                }
            }
            tasksList.remove(id);
        } else {
            System.out.println("Неверно указан идентификатор задачи");
        }

    }

    public void printEpic(int id) {
        if (tasksList.containsKey(id)) {
            if (!(tasksList.get(id) instanceof Epic currentEpic)) {
                System.out.println("Указан неверный Id, задача не имеет тип Epic");
            } else {
                System.out.println(currentEpic);
                System.out.println("Подзадачи:");
                for (Subtask subtask : currentEpic.getEpicSubtasks().values()) {
                    System.out.println("  " + subtask);
                }
                System.out.println();
                System.out.println("-".repeat(11));
                System.out.println();
            }
        } else {
            System.out.println("Указан неверный Id");
        }
    }


}
