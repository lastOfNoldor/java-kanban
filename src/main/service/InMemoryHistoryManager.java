package main.service;

import main.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> historyMap = new HashMap<>();
    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (task != null) {
            if (historyMap.containsKey(task.getId())) {
                remove(task.getId());
            }
            linkLast(new Task(task));
        }
    }

    @Override
    public void remove(int id) {
        Node node = historyMap.get(id);
        removeNode(node);
    }

    private void linkLast(Task task) {
        Node newNode = new Node(task, tail, null);
        if (head == null) {
            head = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
        }
        tail = newNode;
        historyMap.put(task.getId(), newNode);
    }

    private void removeNode(Node node) {
        if (node == null) {
            return;
        }
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
        historyMap.remove(node);
    }

    private List<Task> getTasks() {
        ArrayList<Task> taskList = new ArrayList<>();
        Node current = head;
        while (current != null) {
            taskList.add(current.task);
            current = current.next;
        }
        return taskList;
    }

    @Override
    public List<Task> getHistory() {
        return List.copyOf(getTasks());
    }

    private static class Node {

        Task task;
        Node prev;
        Node next;


        public Node(Task task, Node prev, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }

}

