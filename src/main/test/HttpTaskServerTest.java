package main.test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.model.Epic;
import main.model.Subtask;
import main.model.Task;
import main.model.TaskStatus;
import main.server.HttpTaskServer;
import main.service.Managers;
import main.service.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTest {
    // создаём экземпляр InMemoryTaskManager
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(Managers.getDefault());
    TaskManager manager = taskServer.getTaskManager();
    Gson gson = taskServer.getGson();
    HttpClient client = HttpClient.newHttpClient();

    public HttpTaskServerTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.clearTasks();
        manager.clearSubtasks();
        manager.clearEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void postTaskAndItsExceptions() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks");
        Task task = new Task("Test", "Testing task", LocalDateTime.now(), Duration.ofHours(5));
        String taskJson = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Task> tasksFromManager = manager.getRegularTasksList();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
        Task task2 = new Task("Test2", "Testing task", LocalDateTime.now(), Duration.ofHours(5));
        String taskJson2 = gson.toJson(task2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson2)).build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response2.statusCode());
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(TaskStatus.NEW, manager.getTaskById(1).orElseThrow().getTaskStatus());
        Task updatedTask = manager.getTaskById(1).orElseThrow();
        updatedTask.setTaskStatus(TaskStatus.DONE);
        String taskJson3 = gson.toJson(updatedTask);
        HttpRequest request3 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson3)).build();
        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response3.statusCode());
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(TaskStatus.DONE, manager.getTaskById(1).orElseThrow().getTaskStatus());
    }

    @Test
    public void getTaskAndItsExceptions() throws IOException, InterruptedException {
        Task task = new Task("Test", "Testing task", LocalDateTime.now(), Duration.ofHours(5));
        manager.createTask(task);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Type taskListType = new TypeToken<List<Task>>() {
        }.getType();
        List<Task> tasks = gson.fromJson(response.body(), taskListType);
        assertFalse(tasks.isEmpty());
        assertEquals(1, tasks.size());
        Task task1 = tasks.getFirst();
        assertEquals(task1, manager.getTaskById(1).orElseThrow());
        url = URI.create("http://localhost:8080/tasks/1");
        request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Task task2 = gson.fromJson(response.body(), Task.class);
        assertEquals(task2, manager.getTaskById(1).orElseThrow());
        url = URI.create("http://localhost:8080/tasks/2");
        request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        url = URI.create("http://localhost:8080/tasks/sometext");
        request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
    }

    @Test
    public void deleteTaskAndItsExceptions() throws IOException, InterruptedException {
        Task task = new Task("Test", "Testing task", LocalDateTime.now(), Duration.ofHours(5));
        manager.createTask(task);
        URI url = URI.create("http://localhost:8080/tasks/2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals(1, manager.getRegularTasksList().size());
        url = URI.create("http://localhost:8080/tasks/sometext");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(1, manager.getRegularTasksList().size());
        assertEquals(400, response.statusCode());
        url = URI.create("http://localhost:8080/tasks/1");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(0, manager.getRegularTasksList().size());
    }

    @Test
    public void postSubtaskAndItsExceptions() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/subtasks");
        Epic epic = new Epic("imia", "opisanie");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Test", "Testing task", LocalDateTime.now(), Duration.ofHours(5), epic.getId());
        String subtaskJson = gson.toJson(subtask);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Subtask> tasksFromManager = manager.getSubtasksList();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
        Subtask subtask2 = new Subtask("Test2", "Testing task", LocalDateTime.now(), Duration.ofHours(5), epic.getId());
        String taskJson2 = gson.toJson(subtask2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson2)).build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response2.statusCode());
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(TaskStatus.NEW, manager.getSubtaskById(2).orElseThrow().getTaskStatus());
        Subtask updatedSubtask = manager.getSubtaskById(2).orElseThrow();
        updatedSubtask.setTaskStatus(TaskStatus.DONE);
        String taskJson3 = gson.toJson(updatedSubtask);
        HttpRequest request3 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson3)).build();
        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response3.statusCode());
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(TaskStatus.DONE, manager.getSubtaskById(2).orElseThrow().getTaskStatus());
    }

    @Test
    public void getSubtaskAndItsExceptions() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/subtasks");
        Epic epic = new Epic("imia", "opisanie");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Test", "Testing task", LocalDateTime.now(), Duration.ofHours(5));
        manager.createSubtask(subtask, epic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Type taskListType = new TypeToken<List<Subtask>>() {
        }.getType();
        List<Subtask> subtasks = gson.fromJson(response.body(), taskListType);
        assertFalse(subtasks.isEmpty());
        assertEquals(1, subtasks.size());
        Subtask subtask1 = subtasks.getFirst();
        assertEquals(subtask1, manager.getSubtaskById(2).orElseThrow());
        url = URI.create("http://localhost:8080/subtasks/2");
        request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Subtask subtask2 = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask2, manager.getSubtaskById(2).orElseThrow());
        url = URI.create("http://localhost:8080/subtasks/1");
        request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        url = URI.create("http://localhost:8080/tasks/sometext");
        request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
    }

    @Test
    public void deleteSubtaskAndItsExceptions() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/subtasks");
        Epic epic = new Epic("imia", "opisanie");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Test", "Testing task", LocalDateTime.now(), Duration.ofHours(5));
        manager.createSubtask(subtask, epic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        assertEquals(1, manager.getSubtasksList().size());
        url = URI.create("http://localhost:8080/subtasks/1");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(1, manager.getSubtasksList().size());
        assertEquals(404, response.statusCode());
        url = URI.create("http://localhost:8080/subtasks/2");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(0, manager.getSubtasksList().size());
    }

    @Test
    public void postEpicsAndItsExceptions() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics");
        Epic epic = new Epic("imia", "opisanie");
        String epicJson = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Epic> tasksFromManager = manager.getEpicsList();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("imia", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void getEpicsAndItsExceptions() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics");
        Epic epic = new Epic("imia", "opisanie");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Test", "Testing task", LocalDateTime.now(), Duration.ofHours(5));
        manager.createSubtask(subtask, epic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Type taskListType = new TypeToken<List<Epic>>() {
        }.getType();
        List<Epic> epics = gson.fromJson(response.body(), taskListType);
        assertFalse(epics.isEmpty());
        assertEquals(1, epics.size());
        Epic epic1 = epics.getFirst();
        assertEquals(epic1, manager.getEpicById(1).orElseThrow());
        url = URI.create("http://localhost:8080/epics/1");
        request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Epic epic2 = gson.fromJson(response.body(), Epic.class);
        assertEquals(epic2, manager.getEpicById(1).orElseThrow());
        url = URI.create("http://localhost:8080/epics/2");
        request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        url = URI.create("http://localhost:8080/epics/sometext");
        request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        url = URI.create("http://localhost:8080/epics/1/subtasks");
        request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Type subtasksFromEpicListType = new TypeToken<List<Subtask>>() {
        }.getType();
        List<Subtask> subtasksFromEpic = gson.fromJson(response.body(), subtasksFromEpicListType);
        assertFalse(epics.isEmpty());
        assertEquals(1, subtasksFromEpic.size());
        Subtask resultSubtask = subtasksFromEpic.getFirst();
        assertEquals(resultSubtask, manager.getSubtaskById(2).orElseThrow());
        url = URI.create("http://localhost:8080/epics/2/subtasks");
        request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        url = URI.create("http://localhost:8080/epics/1/subtasksssss");
        request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
    }

    @Test
    public void deleteEpicAndItsExceptions() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics");
        Epic epic = new Epic("imia", "opisanie");
        manager.createEpic(epic);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        assertEquals(1, manager.getEpicsList().size());
        url = URI.create("http://localhost:8080/epics/2");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(1, manager.getEpicsList().size());
        assertEquals(404, response.statusCode());
        url = URI.create("http://localhost:8080/epics/1");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(0, manager.getEpicsList().size());
    }

    @Test
    public void getHistoryListAndItsExceptions() throws IOException, InterruptedException {
        Task task = new Task("Test", "Testing task", LocalDateTime.now(), Duration.ofHours(5));
        manager.createTask(task);
        manager.getTaskById(1);
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Type taskListType = new TypeToken<List<Task>>() {
        }.getType();
        List<Task> tasks = gson.fromJson(response.body(), taskListType);
        assertFalse(tasks.isEmpty());
        assertEquals(1, tasks.size());
        url = URI.create("http://localhost:8080/history/sometext");
        request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());

    }

    @Test
    public void getPrioritizedListAndItsExceptions() throws IOException, InterruptedException {
        Task task = new Task("Test", "Testing task", LocalDateTime.now(), Duration.ofHours(5));
        manager.createTask(task);
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Type taskListType = new TypeToken<List<Task>>() {
        }.getType();
        List<Task> tasks = gson.fromJson(response.body(), taskListType);
        assertFalse(tasks.isEmpty());
        assertEquals(1, tasks.size());
        url = URI.create("http://localhost:8080/prioritized/sometext");
        request = HttpRequest.newBuilder().uri(url).GET().header("Accept", "application/json").build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());

    }

}


