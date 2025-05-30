package main.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import main.service.Managers;
import main.service.TaskManager;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {

    public static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;
    private final Gson gson;


    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        gson = Managers.getGson();
        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        setContext();
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public Gson getGson() {
        return gson;
    }

    private void setContext() {
        TaskHandler taskHandler = new TaskHandler(taskManager, gson);
        server.createContext("/tasks", taskHandler);
        server.createContext("/subtasks", taskHandler);
        server.createContext("/epics", taskHandler);
        server.createContext("/history", taskHandler);
        server.createContext("/prioritized", taskHandler);

    }

    public static void main(String[] args) {
        try {
            HttpTaskServer taskServer = new HttpTaskServer(Managers.getFileBacked(new File("src\\main\\resources\\data.csv")));
            taskServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public void start() {
        System.out.println("Запустили сервер на порту " + PORT);
        System.out.println("http://localhost:" + PORT);
        server.start();
    }

    public void stop() {
        System.out.println("Остановили сервер на порту " + PORT);
        server.stop(0);
    }


}
