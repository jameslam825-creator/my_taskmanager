package com.example.mytaskmanager.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.util.Date;

@Entity(tableName = "task_history")
@TypeConverters({Converters.class})
public class TaskHistory {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int taskId;
    private String action;
    private String description;
    private Date timestamp;

    public TaskHistory() {}

    public TaskHistory(int taskId, String action, String description) {
        this.taskId = taskId;
        this.action = action;
        this.description = description;
        this.timestamp = new Date();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
