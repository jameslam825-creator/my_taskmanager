package com.example.mytaskmanager.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao
public interface TaskHistoryDao {
    @Query("SELECT * FROM task_history ORDER BY timestamp DESC")
    LiveData<List<TaskHistory>> getAllHistory();

    @Insert
    void insert(TaskHistory history);
}
