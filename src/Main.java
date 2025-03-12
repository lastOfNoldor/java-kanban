public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        Task task1 = new Task("Обычное задание#1", "пример описания");
        Task task2 = new Task("Обычное задание#2", "пример описания");
        Task task3 = new Task("Обычное задание#3", "пример описания");
        Epic et1 = new Epic("Эпик#1", "пример описания");
        Subtask subtask1 = new Subtask("Подзадача первого Эпика #1", "пример описания", et1.getId());
        Epic et2 = new Epic("Эпик#2", "пример описания");
        Subtask subtask2 = new Subtask("Подзадача второго Эпика #1", "пример описания", et2.getId());
        Subtask subtask3 = new Subtask("Подзадача второго Эпика #2", "пример описания", et2.getId());

        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);
        taskManager.createTask(et1);
        taskManager.createTask(et2);
        taskManager.createTask(subtask1);
        taskManager.createTask(subtask2);
        taskManager.createTask(subtask3);

        subtask1.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(subtask1);

        taskManager.printTasks();
        taskManager.printEpic(et1.getId());

    }
}
