package com.example.mytaskmanager.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.mytaskmanager.data.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskRepository {
    private TaskDao taskDao;
    private TaskHistoryDao taskHistoryDao;
    private Executor executor;

    public TaskRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        taskDao = database.taskDao();
        taskHistoryDao = database.taskHistoryDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Task>> getAllTasks() {
        return taskDao.getAllTasks();
    }

    public LiveData<List<Task>> getTasksByStatus(String status) {
        return taskDao.getTasksByStatus(status);
    }

    public LiveData<List<Task>> getTasksDueBetween(Date start, Date end) {
        return taskDao.getTasksDueBetween(start, end);
    }

    public void insertTask(Task task) {
        executor.execute(() -> {
            taskDao.insert(task);
            TaskHistory history = new TaskHistory(task.getId(), "created", 
                "Task '" + task.getTitle() + "' created");
            taskHistoryDao.insert(history);
        });
    }

    public void updateTask(Task task) {
        executor.execute(() -> {
            taskDao.update(task);
            TaskHistory history = new TaskHistory(task.getId(), "updated", 
                "Task '" + task.getTitle() + "' updated");
            taskHistoryDao.insert(history);
        });
    }

    public void deleteTask(Task task) {
        executor.execute(() -> {
            taskDao.delete(task);
            TaskHistory history = new TaskHistory(task.getId(), "deleted", 
                "Task '" + task.getTitle() + "' deleted");
            taskHistoryDao.insert(history);
        });
    }

    public LiveData<List<TaskHistory>> getAllHistory() {
        return taskHistoryDao.getAllHistory();
    }
}
