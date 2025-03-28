package main.service;

import main.model.Task;
import java.util.ArrayList;
import java.util.List;


public class InMemoryHistoryManager implements  HistoryManager{
    private final List<Task> historyList;

    public InMemoryHistoryManager() {
        this.historyList =  new ArrayList<>();
    }

    @Override
    public void add(Task task) {
        if (task != null) {
            if (historyList.size() == 10) {
                historyList.removeFirst();
            }
            historyList.add(task);
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyList);

    }
}
