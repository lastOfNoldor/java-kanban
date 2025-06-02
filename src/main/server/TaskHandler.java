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
import java.util.*;

import static java.net.HttpURLConnection.*;

public class TaskHandler implements HttpHandler {
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_ERROR = "error";
    private static final int ONLY_TASK_TYPE = 2;
    private static final int TASK_TYPE_WITH_ID = 3;
    private final TaskManager taskManager;
    private final Gson gson;
    private int code;
    private String[] pathParts;
    private HttpMethod method;


    public TaskHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        pathParts = exchange.getRequestURI().getPath().split("/");
        Endpoint endpoint = getEndpoint();
        method = HttpMethod.valueOf(exchange.getRequestMethod());
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
            case GET -> getRequestTasks();
            case POST -> postRequestTask(exchange);
            case DELETE -> deleteRequestTask();
        };
    }

    private String parseSubtasksRequest(HttpExchange exchange) throws IOException {
        return switch (method) {
            case GET -> getRequestSubtask();
            case POST -> postRequestSubtask(exchange);
            case DELETE -> deleteRequestSubtask();

        };

    }

    private String parseEpicsRequest(HttpExchange exchange) throws IOException {
        return switch (method) {
            case GET -> getRequestEpic();
            case POST -> postRequestEpic(exchange);
            case DELETE -> deleteRequestEpic();
        };
    }

    private String parseHistoryRequest() {
        if (pathParts.length == 2 && method == HttpMethod.GET) {
            List<Task> historyList = taskManager.getHistoryManager().getHistory();
            if (historyList.isEmpty()) {
                return emptyList();
            } else {
                return getSuccess(historyList);
            }
        }
        return badRequest();
    }

    private String parsePrioritizedRequest() {
        if (pathParts.length == 2 && method == HttpMethod.GET) {
            TreeSet<Task> prioritizedList = taskManager.getPrioritizedTasks();
            if (prioritizedList.isEmpty()) {
                return emptyList();
            } else {
                return getSuccess(prioritizedList);
            }
        }
        return badRequest();
    }

    private String getRequestTasks() {
        if (pathParts.length == ONLY_TASK_TYPE) {
            return tasksListRequest();
        } else if (pathParts.length == TASK_TYPE_WITH_ID) {
            return taskSingleRequest();
        }
        return badRequest();
    }

    private String tasksListRequest() {
        List<Task> taskList = taskManager.getRegularTasksList();
        if (taskList.isEmpty()) {
            return emptyList();
        } else {
            return getSuccess(taskList);
        }
    }

    private String taskSingleRequest() {
        try {
            int id = Integer.parseInt(pathParts[2]);
            Optional<Task> task = taskManager.getTaskById(id);
            if (task.isPresent()) {
                return getSuccess(task.get());
            }
            return notFound();
        } catch (NumberFormatException e) {
            return badRequest();
        }
    }

    private String postRequestTask(HttpExchange exchange) throws IOException {
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
                return notAcceptable(e.getMessage());
            }
        } catch (JsonParseException | IllegalArgumentException e) {
            return badRequestCustomMessage(e.getMessage());
        }
    }

    private String deleteRequestTask() {
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

    private String getRequestSubtask() {
        if (pathParts.length == ONLY_TASK_TYPE) {
            return subtasksListRequest();
        } else if (pathParts.length == TASK_TYPE_WITH_ID) {
            return subtaskSingleRequest();
        } else {
            return badRequest();
        }
    }

    private String subtasksListRequest() {
        List<Subtask> subtaskList = taskManager.getSubtasksList();
        if (subtaskList.isEmpty()) {
            return emptyList();
        } else {
            return getSuccess(subtaskList);
        }
    }

    private String subtaskSingleRequest() {
        try {
            int id = Integer.parseInt(pathParts[2]);
            Optional<Subtask> subtask = taskManager.getSubtaskById(id);
            if (subtask.isPresent()) {
                return getSuccess(subtask.get());
            }
            return notFound();
        } catch (NumberFormatException e) {
            return badRequest();
        }
    }

    private String postRequestSubtask(HttpExchange exchange) throws IOException {
        if (pathParts.length != ONLY_TASK_TYPE) {
            return badRequest();
        }
        try {
            String json = readText(exchange);
            Subtask subtask = gson.fromJson(json, Subtask.class);
            subtask.validate();
            try {
                if (subtask.getId() == 0) {
                    if (taskManager.getEpicById(subtask.getEpicId()).isEmpty()) {
                        return badRequestCustomMessage("Неверно указан id Эпика к которому принадлежит подзадача. Такого Эпика нет");
                    }
                    taskManager.createSubtask(subtask, subtask.getEpicId());
                    return createSuccess();
                } else {
                    if (taskManager.getSubtaskById(subtask.getId()).isEmpty()) {
                        return notFound();
                    }
                    taskManager.updateSubtask(subtask);
                    return updateSuccess();
                }
            } catch (IllegalTaskTimeException e) {
                return notAcceptable(e.getMessage());
            }
        } catch (JsonParseException | IllegalArgumentException e) {
            return badRequestCustomMessage(e.getMessage());
        }
    }

    private String deleteRequestSubtask() {
        if (pathParts.length != TASK_TYPE_WITH_ID) {
            return badRequest();
        }
        try {
            int id = Integer.parseInt(pathParts[2]);
            if (taskManager.getSubtaskById(id).isPresent()) {
                taskManager.deleteSubtask(id);
                return deleteSuccess();
            }
            return notFound();
        } catch (NumberFormatException e) {
            return badRequest();
        }
    }

    private String getRequestEpic() {
        try {
            if (pathParts.length == ONLY_TASK_TYPE) {
                return epicsListRequest();
            } else if (pathParts.length == TASK_TYPE_WITH_ID) {
                return epicSingleRequest();
            } else if ((pathParts.length == 4) && (pathParts[3].equals("subtasks"))) {
                return epicSubtasksRequest();
            } else {
                return badRequest();
            }
        } catch (NumberFormatException e) {
            return badRequest();
        }
    }


    private String epicsListRequest() {
        List<Epic> epicList = taskManager.getEpicsList();
        if (epicList.isEmpty()) {
            return emptyList();
        } else {
            return getSuccess(epicList);
        }
    }

    private String epicSingleRequest() {
        int id = Integer.parseInt(pathParts[2]);
        Optional<Epic> epic = taskManager.getEpicById(id);
        if (epic.isPresent()) {
            return getSuccess(epic.get());
        }
        return notFound();
    }

    private String epicSubtasksRequest() {
        int id = Integer.parseInt(pathParts[2]);
        Optional<Epic> epic = taskManager.getEpicById(id);
        if (epic.isPresent()) {
            List<Subtask> epicSubtasks = epic.get().getEpicSubtasks().stream().map(subtaskID -> taskManager.getSubtaskById(subtaskID).orElseThrow()).toList();
            if (epicSubtasks.isEmpty()) {
                return emptyEpic();
            }
            return getSuccess(epicSubtasks);
        }
        return notFound();
    }

    private String postRequestEpic(HttpExchange exchange) throws IOException {
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
                    return createSuccess();
                } else {
                    return badRequest();
                }
            } catch (IllegalTaskTimeException e) {
                return notAcceptable(e.getMessage());
            }
        } catch (JsonParseException | IllegalArgumentException e) {
            return badRequestCustomMessage(e.getMessage());
        }

    }

    private String deleteRequestEpic() {
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

    private String notAcceptable(String customMessage) {
        return new JsonResponseBuilder().add(KEY_ERROR, customMessage).status(HTTP_NOT_ACCEPTABLE).build(gson);
    }

    private String emptyList() {
        return new JsonResponseBuilder().add(KEY_MESSAGE, "Cписок на данный момент пуст.").status(HTTP_OK).build(gson);
    }

    private String emptyEpic() {
        return new JsonResponseBuilder().add(KEY_MESSAGE, "В данном Эпике пока что нет подзадач").status(HTTP_OK).build(gson);
    }

    private String badRequest() {
        return new JsonResponseBuilder().add(KEY_ERROR, "Неверный запрос.").status(HTTP_BAD_REQUEST).build(gson);
    }

    private String badRequestCustomMessage(String customMessage) {
        return new JsonResponseBuilder().add(KEY_ERROR, "Ошибка в данных: " + customMessage).status(HTTP_BAD_REQUEST).build(gson);
    }

    private String notFound() {
        return new JsonResponseBuilder().add(KEY_ERROR, "Неверно указан id или класс задачи.").status(HTTP_NOT_FOUND).build(gson);
    }

    private String pageNotFound() {
        return new JsonResponseBuilder().add(KEY_ERROR, "Такого адреса не существует").status(HTTP_NOT_FOUND).build(gson);
    }

    private String deleteSuccess() {
        return new JsonResponseBuilder().add(KEY_MESSAGE, "Задача успешно удалена.").status(HTTP_OK).build(gson);
    }

    private String updateSuccess() {
        return new JsonResponseBuilder().add(KEY_MESSAGE, "Задача успешно обновлена.").status(HTTP_CREATED).build(gson);
    }

    private String createSuccess() {
        return new JsonResponseBuilder().add(KEY_MESSAGE, "Задача успешно создана.").status(HTTP_CREATED).build(gson);
    }

    private String getSuccess(Object responseObject) {
        return new JsonResponseBuilder().addObject(responseObject).status(HTTP_OK).build(gson);
    }

    enum HttpMethod {
        GET, POST, DELETE
    }

    enum Endpoint {
        TASKS, SUBTASKS, EPICS, HISTORY, UNKNOWN, PRIORITIZED
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

    public class JsonResponseBuilder {
        private final Map<String, Object> data = new HashMap<>();
        private Object responseObject;
        private int statusCode;

        public JsonResponseBuilder add(String key, Object value) {
            data.put(key, value);
            return this;
        }

        public JsonResponseBuilder status(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public String build(Gson gson) {
            code = statusCode;
            return gson.toJson(Objects.requireNonNullElse(responseObject, data));
        }

        public JsonResponseBuilder addObject(Object object) {
            responseObject = object;
            return this;
        }

    }

}