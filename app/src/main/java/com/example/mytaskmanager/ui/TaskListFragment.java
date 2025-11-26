package com.example.mytaskmanager.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mytaskmanager.R;
import com.example.mytaskmanager.adapter.TaskAdapter;
import com.example.mytaskmanager.data.Task;
import com.example.mytaskmanager.viewmodel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TaskListFragment extends Fragment {
    private TaskViewModel taskViewModel;
    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private TextView emptyText;
    private List<Task> allTasks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);
        
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        emptyText = view.findViewById(R.id.emptyText);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
        
        setupRecyclerView();
        setupClickListeners(fabAdd);
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        
        taskViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            allTasks = tasks != null ? tasks : new ArrayList<>();
            taskAdapter.updateTasks(allTasks);
            if (tasks == null || tasks.isEmpty()) {
                emptyText.setVisibility(View.VISIBLE);
                tasksRecyclerView.setVisibility(View.GONE);
            } else {
                emptyText.setVisibility(View.GONE);
                tasksRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(new ArrayList<>(), new TaskAdapter.TaskActionListener() {
            @Override
            public void onEditTask(Task task) {
                showEditTaskDialog(task);
            }

            @Override
            public void onDeleteTask(Task task) {
                showDeleteConfirmation(task);
            }

            @Override
            public void onStatusChange(Task task, String newStatus) {
                task.setStatus(newStatus);
                taskViewModel.updateTask(task);
            }
        });
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tasksRecyclerView.setAdapter(taskAdapter);
    }

    private void setupClickListeners(FloatingActionButton fabAdd) {
        fabAdd.setOnClickListener(v -> showAddTaskDialog());
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_task, null);

        EditText titleInput = dialogView.findViewById(R.id.titleInput);
        EditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);
        Spinner statusSpinner = dialogView.findViewById(R.id.statusSpinner);
        Spinner prioritySpinner = dialogView.findViewById(R.id.prioritySpinner);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.status_array, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.priority_array, android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(priorityAdapter);

        builder.setView(dialogView)
                .setTitle("Add New Task")
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    String description = descriptionInput.getText().toString().trim();
                    String status = statusSpinner.getSelectedItem().toString().toLowerCase();
                    int priority = prioritySpinner.getSelectedItemPosition() + 1;

                    if (!title.isEmpty()) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                        Date dueDate = calendar.getTime();

                        Task task = new Task(title, description, dueDate, status, priority, "General");
                        taskViewModel.insertTask(task);
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void showEditTaskDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_task, null);

        EditText titleInput = dialogView.findViewById(R.id.titleInput);
        EditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);
        Spinner statusSpinner = dialogView.findViewById(R.id.statusSpinner);
        Spinner prioritySpinner = dialogView.findViewById(R.id.prioritySpinner);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.status_array, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.priority_array, android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(priorityAdapter);

        titleInput.setText(task.getTitle());
        descriptionInput.setText(task.getDescription());

        String[] statusArray = getResources().getStringArray(R.array.status_array);
        for (int i = 0; i < statusArray.length; i++) {
            if (statusArray[i].equalsIgnoreCase(task.getStatus())) {
                statusSpinner.setSelection(i);
                break;
            }
        }
        prioritySpinner.setSelection(task.getPriority() - 1);

        builder.setView(dialogView)
                .setTitle("Edit Task")
                .setPositiveButton("Update", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    String description = descriptionInput.getText().toString().trim();
                    String status = statusSpinner.getSelectedItem().toString().toLowerCase();
                    int priority = prioritySpinner.getSelectedItemPosition() + 1;

                    if (!title.isEmpty()) {
                        task.setTitle(title);
                        task.setDescription(description);
                        task.setStatus(status);
                        task.setPriority(priority);
                        taskViewModel.updateTask(task);
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void showDeleteConfirmation(Task task) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> taskViewModel.deleteTask(task))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
