package com.example.mytaskmanager.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mytaskmanager.R;
import com.example.mytaskmanager.data.TaskHistory;
import com.example.mytaskmanager.viewmodel.TaskViewModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {
    private TaskViewModel taskViewModel;
    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;
    private TextView emptyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView);
        emptyText = view.findViewById(R.id.emptyText);
        
        setupRecyclerView();
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        
        taskViewModel.getAllHistory().observe(getViewLifecycleOwner(), history -> {
            historyAdapter.updateHistory(history != null ? history : new ArrayList<>());
            if (history == null || history.isEmpty()) {
                emptyText.setVisibility(View.VISIBLE);
                historyRecyclerView.setVisibility(View.GONE);
            } else {
                emptyText.setVisibility(View.GONE);
                historyRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupRecyclerView() {
        historyAdapter = new HistoryAdapter(new ArrayList<>());
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        historyRecyclerView.setAdapter(historyAdapter);
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
        private List<TaskHistory> history;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

        public HistoryAdapter(List<TaskHistory> history) {
            this.history = history;
        }

        public void updateHistory(List<TaskHistory> history) {
            this.history = history;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history, parent, false);
            return new HistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
            TaskHistory historyItem = history.get(position);
            holder.bind(historyItem);
        }

        @Override
        public int getItemCount() {
            return history.size();
        }

        static class HistoryViewHolder extends RecyclerView.ViewHolder {
            TextView actionText, descriptionText, timestampText;

            HistoryViewHolder(@NonNull View itemView) {
                super(itemView);
                actionText = itemView.findViewById(R.id.actionText);
                descriptionText = itemView.findViewById(R.id.descriptionText);
                timestampText = itemView.findViewById(R.id.timestampText);
            }

            void bind(TaskHistory history) {
                actionText.setText(history.getAction());
                descriptionText.setText(history.getDescription());
                timestampText.setText(dateFormat.format(history.getTimestamp()));
            }
        }
    }
}
