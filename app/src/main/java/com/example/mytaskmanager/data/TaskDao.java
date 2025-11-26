package com.example.mytaskmanager.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY dueDate ASC")
    LiveData<List<Task>> getTasksByStatus(String status);

    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :start AND :end ORDER BY dueDate ASC")
    LiveData<List<Task>> getTasksDueBetween(Date start, Date end);

    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);
}
