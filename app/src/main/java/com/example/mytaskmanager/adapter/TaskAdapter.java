package com.example.mytaskmanager.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mytaskmanager.R;
import com.example.mytaskmanager.data.Task;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;
    private TaskActionListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public TaskAdapter(List<Task> tasks, TaskActionListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    public void updateTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDescription, taskDueDate, taskStatus, taskPriority;
        Button editButton, deleteButton;
        View priorityIndicator;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskDueDate = itemView.findViewById(R.id.taskDueDate);
            taskStatus = itemView.findViewById(R.id.taskStatus);
            taskPriority = itemView.findViewById(R.id.taskPriority);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);
        }

        void bind(Task task) {
            taskTitle.setText(task.getTitle());
            taskDescription.setText(task.getDescription());
            
            if (task.getDueDate() != null) {
                taskDueDate.setText("Due: " + dateFormat.format(task.getDueDate()));
            } else {
                taskDueDate.setText("No due date");
            }
            
            String status = task.getStatus();
            taskStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
            
            String priorityText = getPriorityText(task.getPriority());
            taskPriority.setText(priorityText);
            
            int priorityColor = getPriorityColor(task.getPriority());
            priorityIndicator.setBackgroundColor(priorityColor);
            
            int statusColor = getStatusColor(task.getStatus());
            taskStatus.setTextColor(statusColor);

            editButton.setOnClickListener(v -> listener.onEditTask(task));
            deleteButton.setOnClickListener(v -> listener.onDeleteTask(task));

            itemView.setOnClickListener(v -> {
                String newStatus = getNextStatus(task.getStatus());
                listener.onStatusChange(task, newStatus);
            });
        }

        private String getPriorityText(int priority) {
            switch (priority) {
                case 3: return "High";
                case 2: return "Medium";
                default: return "Low";
            }
        }

        private int getPriorityColor(int priority) {
            switch (priority) {
                case 3: return Color.RED;
                case 2: return Color.YELLOW;
                default: return Color.GREEN;
            }
        }

        private int getStatusColor(String status) {
            switch (status) {
                case "incomplete": return Color.RED;
                case "in progress": return Color.BLUE;
                case "complete": return Color.GREEN;
                default: return Color.GRAY;
            }
        }

        private String getNextStatus(String currentStatus) {
            switch (currentStatus) {
                case "incomplete": return "in progress";
                case "in progress": return "complete";
                case "complete": return "incomplete";
                default: return "incomplete";
            }
        }
    }

    public interface TaskActionListener {
        void onEditTask(Task task);
        void onDeleteTask(Task task);
        void onStatusChange(Task task, String newStatus);
    }
}
