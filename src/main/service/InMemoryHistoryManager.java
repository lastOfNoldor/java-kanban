package main.service;

import main.model.Task;
import java.util.ArrayList;
import java.util.List;


public class InMemoryHistoryManager implements  HistoryManager{
    private final List<Task> historyList = new ArrayList<>();
    private static final int HISTORY_MAX_CAPACITY = 10;


    @Override
    public void add(Task task) {
        if (task != null) {
            if (historyList.size() >= HISTORY_MAX_CAPACITY) {
                historyList.removeFirst();
            }
            historyList.add(task);
        }
    }

    @Override
    public List<Task> getHistory() {
        return List.copyOf(historyList);

    }
}
