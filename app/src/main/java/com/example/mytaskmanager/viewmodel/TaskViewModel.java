package com.example.mytaskmanager.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.mytaskmanager.data.Task;
import com.example.mytaskmanager.data.TaskHistory;
import com.example.mytaskmanager.repository.TaskRepository;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private TaskRepository repository;
    private LiveData<List<Task>> allTasks;
    private LiveData<List<TaskHistory>> allHistory;

    public TaskViewModel(Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();
        allHistory = repository.getAllHistory();
    }

    public LiveData<List<Task>> getAllTasks() { return allTasks; }
    public LiveData<List<TaskHistory>> getAllHistory() { return allHistory; }

    public LiveData<List<Task>> getTasksByStatus(String status) {
        return repository.getTasksByStatus(status);
    }

    public LiveData<List<Task>> getUpcomingTasks() {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.HOUR, 48);
        Date in48Hours = calendar.getTime();
        return repository.getTasksDueBetween(now, in48Hours);
    }

    public void insertTask(Task task) {
        repository.insertTask(task);
    }

    public void updateTask(Task task) {
        repository.updateTask(task);
    }

    public void deleteTask(Task task) {
        repository.deleteTask(task);
    }
}
