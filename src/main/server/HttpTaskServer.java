package main.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import main.service.Managers;
import main.service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {

    public static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;
    private final Gson gson;

    public HttpTaskServer() throws IOException {
        taskManager = Managers.getDefault(); //TODO инициализировать gson, какой таск менеджер поставить.

        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        setContext();
    }

    private void setContext() {
        TaskHandler taskHandler = new TaskHandler(taskManager, gson);
        server.createContext("/tasks", taskHandler);
        server.createContext("/subtasks", taskHandler);
        server.createContext("/epics", taskHandler);
        server.createContext("/history", taskHandler);
        server.createContext("/prioritized", taskHandler);

    }


}
