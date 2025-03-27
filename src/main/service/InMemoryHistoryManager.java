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
        historyList.add(task);
    }

    @Override
    public List<Task> getHistory() {
        if (historyList.size() > 10) {
            List<Task> returnList = new ArrayList<>();
            for (int i = 10; i > 0; i--) {
                returnList.add(historyList.get(historyList.size() - i));
            }
            return returnList;
        } else {
            return historyList;
        }

    }
}
