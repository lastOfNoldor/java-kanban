package main.server;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.model.Epic;
import main.model.Subtask;
import main.model.Task;
import main.service.IllegalTaskTimeException;
import main.service.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TaskHandler implements HttpHandler {
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_ERROR = "error";
    private static final int ONLY_TASK_TYPE = 2;
    private static final int TASK_TYPE_WITH_ID = 3;
    TaskManager taskManager;
    Gson gson;
    int code;
    String[] pathParts;
    String method;

    public TaskHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        pathParts = exchange.getRequestURI().getPath().split("/");
        Endpoint endpoint = getEndpoint();
        method = exchange.getRequestMethod();
        String response;

        switch (endpoint) {
            case TASKS -> response = parseTasksRequest(exchange);
            case SUBTASKS -> response = parseSubtasksRequest(exchange);
            case EPICS -> response = parseEpicsRequest(exchange);
            case HISTORY -> response = parseHistoryRequest();
            case PRIORITIZED -> response = parsePrioritizedRequest();
            default -> response = pageNotFound();
        }
        sendText(exchange, response, code);
    }


    private String readText(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private void sendText(HttpExchange exchange, String text, int code) throws IOException {
        byte[] responseBytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(code, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private String parseTasksRequest(HttpExchange exchange) throws IOException {
        return switch (method) {
            case "GET" -> tasksRequestGet();
            case "POST" -> tasksRequestPost(exchange);
            case "DELETE" -> tasksRequestDelete();
            default -> badRequest();
        };
    }

    private String parseSubtasksRequest(HttpExchange exchange) throws IOException {
        return switch (method) {
            case "GET" -> subtasksRequestGet();
            case "POST" -> subtasksRequestPost(exchange);
            case "DELETE" -> subtasksRequestDelete();
            default -> badRequest();
        };

    }

    private String parseEpicsRequest(HttpExchange exchange) throws IOException {
        return switch (method) {
            case "GET" -> epicsRequestGet();
            case "POST" -> epicsRequestPost(exchange);
            case "DELETE" -> epicsRequestDelete();
            default -> badRequest();
        };
    }

    private String parseHistoryRequest() {
        if (pathParts.length == 2 && method.equals("GET")) {
            code = 200;
            return gson.toJson(taskManager.getHistoryManager().getHistory());
        }
        return badRequest();
    }

    private String parsePrioritizedRequest() {
        if (pathParts.length == 2 && method.equals("GET")) {
            code = 200;
            return gson.toJson(taskManager.getPrioritizedTasks());
        }
        return badRequest();
    }

    private String tasksRequestGet() {
        if (pathParts.length == ONLY_TASK_TYPE) {
            code = 200;
            return gson.toJson(taskManager.getRegularTasksList());
        } else if (pathParts.length == TASK_TYPE_WITH_ID) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                Optional<Task> task = taskManager.getTaskById(id);
                if (task.isPresent()) {
                    code = 200;
                    return gson.toJson(task.get());
                }
                return notFound();
            } catch (NumberFormatException e) {
                badRequest();
            }
        } else {
            return badRequest();
        }
    }

    private String tasksRequestPost(HttpExchange exchange) throws IOException {
        if (pathParts.length != ONLY_TASK_TYPE) {
            return badRequest();
        }
        try {
            String json = readText(exchange);
            Task task = gson.fromJson(json, Task.class);
            task.validate();
            try {
                if (task.getId() == 0) {
                    taskManager.createTask(task);
                    return createSuccess();
                } else {
                    if (taskManager.getTaskById(task.getId()).isEmpty()) {
                        return notFound();
                    }
                    taskManager.updateTask(task);
                    return updateSuccess();
                }
            } catch (IllegalTaskTimeException e) {
                code = 406;
                return createJsonResponse(KEY_ERROR, e.getMessage());
            }
        } catch (JsonParseException | IllegalArgumentException e) {
            code = 400;
            return createJsonResponse(KEY_ERROR, "Ошибка в данных: " + e.getMessage());
        }
    }

    private String tasksRequestDelete() {
        if (pathParts.length != TASK_TYPE_WITH_ID) {
            return badRequest();
        }
        try {
            int id = Integer.parseInt(pathParts[2]);
            if (taskManager.getTaskById(id).isPresent()) {
                taskManager.deleteTask(id);
                return deleteSuccess();
            }
            return notFound();
        } catch (NumberFormatException e) {
            return badRequest();
        }
    }

    private String subtasksRequestGet() {
        if (pathParts.length == ONLY_TASK_TYPE) {
            code = 200;
            return gson.toJson(taskManager.getSubTasksTasksList());
        } else if (pathParts.length == TASK_TYPE_WITH_ID) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                Optional<Subtask> subtask = taskManager.getSubTaskById(id);
                if (subtask.isPresent()) {
                    code = 200;
                    return gson.toJson(subtask.get());
                }
                return notFound();
            } catch (NumberFormatException e) {
                return badRequest();
            }
        } else {
            return badRequest();
        }
    }

    private String subtasksRequestPost(HttpExchange exchange) throws IOException {
        if (pathParts.length != ONLY_TASK_TYPE) {
            return badRequest();
        }
        try {
            String json = readText(exchange);
            Subtask subtask = gson.fromJson(json, Subtask.class);
            subtask.validate();
            try {
                if (subtask.getId() == 0) {
                    if (taskManager.getEpicById(subtask.getId()).isEmpty()) {
                        code = 400;
                        return createJsonResponse(KEY_ERROR, "Неверно указан id Эпика к которому принадлежит подзадача. Такого Эпика нет");
                    }
                    taskManager.createSubTask(subtask, subtask.getEpicId());
                    return createSuccess();
                } else {
                    if (taskManager.getSubTaskById(subtask.getId()).isEmpty()) {
                        return notFound();
                    }
                    taskManager.updateSubTask(subtask);
                    return updateSuccess();
                }
            } catch (IllegalTaskTimeException e) {
                code = 406;
                return createJsonResponse(KEY_ERROR, e.getMessage());
            }
        } catch (JsonParseException | IllegalArgumentException e) {
            code = 400;
            return createJsonResponse(KEY_ERROR, "Ошибка в данных: " + e.getMessage());
        }
    }

    private String subtasksRequestDelete() {
        if (pathParts.length != TASK_TYPE_WITH_ID) {
            return badRequest();
        }
        try {
            int id = Integer.parseInt(pathParts[2]);
            if (taskManager.getTaskById(id).isPresent()) {
                taskManager.deleteSubTask(id);
                return deleteSuccess();
            }
            return notFound();
        } catch (NumberFormatException e) {
            return badRequest();
        }
    }

    private String epicsRequestGet() {
        try {
            if (pathParts.length == ONLY_TASK_TYPE) {
                code = 200;
                return gson.toJson(taskManager.getEpicTasksList());
            } else if (pathParts.length == TASK_TYPE_WITH_ID) {
                int id = Integer.parseInt(pathParts[2]);
                Optional<Epic> epic = taskManager.getEpicById(id);
                if (epic.isPresent()) {
                    code = 200;
                    return gson.toJson(epic.get());
                }
                return notFound();
            } else if ((pathParts.length == 4) && (pathParts[3].equals("subtasks"))) {
                int id = Integer.parseInt(pathParts[2]);
                Optional<Epic> epic = taskManager.getEpicById(id);
                if (epic.isPresent()) {
                    code = 200;
                    List<Subtask> epicSubtasks = epic.get().getEpicSubtasks().stream().map(subtaskID -> taskManager.getSubTaskById(subtaskID).get()).toList();
                    if (epicSubtasks.isEmpty()) {
                        return createJsonResponse(KEY_MESSAGE, "В данном Эпике пока что нет подзадач");
                    }
                    return gson.toJson(epicSubtasks);
                }
                return notFound();
            } else {
                return badRequest();
            }
        } catch (NumberFormatException e) {
            return badRequest();
        }
    }

    private String epicsRequestPost(HttpExchange exchange) throws IOException {
        if (pathParts.length != ONLY_TASK_TYPE) {
            return badRequest();
        }
        try {
            String json = readText(exchange);
            Epic epic = gson.fromJson(json, Epic.class);
            epic.validate();
            try {
                if (epic.getId() == 0) {
                    taskManager.createEpic(epic);
                    createSuccess();
                } else {
                    return badRequest();
                }
            } catch (IllegalTaskTimeException e) {
                code = 406;
                return createJsonResponse(KEY_ERROR, e.getMessage());
            }
        } catch (JsonParseException | IllegalArgumentException e) {
            code = 400;
            return createJsonResponse(KEY_ERROR, "Ошибка в данных: " + e.getMessage());
        }
    }

    private String epicsRequestDelete() {
        if (pathParts.length != TASK_TYPE_WITH_ID) {
            return badRequest();
        }
        try {
            int id = Integer.parseInt(pathParts[2]);
            if (taskManager.getEpicById(id).isPresent()) {
                taskManager.deleteEpic(id);
                return deleteSuccess();
            }
            return notFound();
        } catch (NumberFormatException e) {
            return badRequest();
        }
    }


    private String badRequest() {
        code = 400;
        return createJsonResponse(KEY_ERROR, "Неверный запрос.");
    }

    private String notFound() {
        code = 404;
        return createJsonResponse(KEY_ERROR, "Неверно указан id или класс задачи.");
    }

    private String pageNotFound() {
        code = 404;
        return createJsonResponse(KEY_ERROR, "Такого адреса не существует");
    }

    private String deleteSuccess() {
        code = 200;
        return createJsonResponse(KEY_MESSAGE, "Задача успешно удалена!");
    }

    private String updateSuccess() {
        code = 201;
        return createJsonResponse(KEY_MESSAGE, "Задача успешно обновлена.");
    }

    private String createSuccess() {
        code = 201;
        return createJsonResponse(KEY_MESSAGE, "Задача успешно создана!.");
    }

    enum Endpoint {
        TASKS, SUBTASKS, EPICS, HISTORY, UNKNOWN, PRIORITIZED
    }

    private String createJsonResponse(String key, String value) {
        return gson.toJson(Map.of(key, value));
    }

    private Endpoint getEndpoint() {
        return switch (pathParts[1]) {
            case "tasks" -> Endpoint.TASKS;
            case "subtasks" -> Endpoint.SUBTASKS;
            case "epics" -> Endpoint.EPICS;
            case "history" -> Endpoint.HISTORY;
            case "prioritized" -> Endpoint.PRIORITIZED;
            default -> Endpoint.UNKNOWN;
        };

    }

}