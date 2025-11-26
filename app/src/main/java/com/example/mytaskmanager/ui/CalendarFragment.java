package com.example.mytaskmanager.ui;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mytaskmanager.R;
import com.example.mytaskmanager.data.Task;
import com.example.mytaskmanager.viewmodel.TaskViewModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {
    private TaskViewModel taskViewModel;
    private RecyclerView calendarRecyclerView;
    private TextView monthYearText;
    private Calendar currentCalendar;
    private List<Task> allTasks = new ArrayList<>();
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        monthYearText = view.findViewById(R.id.monthYearText);
        
        currentCalendar = Calendar.getInstance();
        setupCalendar();
        setupClickListeners(view);
        setupMultimedia();
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        
        taskViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            allTasks = tasks != null ? tasks : new ArrayList<>();
            updateCalendar();
            checkUpcomingDeadlines();
        });
    }

    private void setupCalendar() {
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        updateCalendar();
    }

    private void setupClickListeners(View view) {
        Button prevMonth = view.findViewById(R.id.prevMonth);
        Button nextMonth = view.findViewById(R.id.nextMonth);
        Button currentMonth = view.findViewById(R.id.currentMonth);

        prevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        nextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        currentMonth.setOnClickListener(v -> {
            currentCalendar = Calendar.getInstance();
            updateCalendar();
        });
    }

    private void setupMultimedia() {
        mediaPlayer = MediaPlayer.create(getContext(), R.raw.notification_sound);
        vibrator = (Vibrator) requireContext().getSystemService(android.content.Context.VIBRATOR_SERVICE);
    }

    private void updateCalendar() {
        monthYearText.setText(monthYearFormat.format(currentCalendar.getTime()));
        
        List<CalendarDay> days = generateCalendarDays();
        CalendarAdapter adapter = new CalendarAdapter(days);
        calendarRecyclerView.setAdapter(adapter);
    }

    private List<CalendarDay> generateCalendarDays() {
        List<CalendarDay> days = new ArrayList<>();

        Calendar calendar = (Calendar) currentCalendar.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i < firstDayOfWeek; i++) {
            days.add(new CalendarDay(null, false, false, new ArrayList<>(), 0));
        }

        for (int day = 1; day <= daysInMonth; day++) {
            Calendar dayCalendar = (Calendar) calendar.clone();
            dayCalendar.set(Calendar.DAY_OF_MONTH, day);
            Date date = dayCalendar.getTime();
            
            List<Task> dayTasks = getTasksForDate(date);
            boolean hasTasks = !dayTasks.isEmpty();
            boolean hasUrgentTasks = hasUrgentTasks(dayTasks);
            
            days.add(new CalendarDay(date, hasTasks, hasUrgentTasks, dayTasks, day));
        }

        return days;
    }

    private List<Task> getTasksForDate(Date date) {
        List<Task> tasksForDate = new ArrayList<>();
        Calendar targetDate = Calendar.getInstance();
        targetDate.setTime(date);
        
        for (Task task : allTasks) {
            if (task.getDueDate() != null) {
                Calendar taskDate = Calendar.getInstance();
                taskDate.setTime(task.getDueDate());
                
                if (targetDate.get(Calendar.YEAR) == taskDate.get(Calendar.YEAR) &&
                    targetDate.get(Calendar.MONTH) == taskDate.get(Calendar.MONTH) &&
                    targetDate.get(Calendar.DAY_OF_MONTH) == taskDate.get(Calendar.DAY_OF_MONTH)) {
                    tasksForDate.add(task);
                }
            }
        }
        return tasksForDate;
    }

    private boolean hasUrgentTasks(List<Task> tasks) {
        for (Task task : tasks) {
            if (task.getPriority() == 3 || "incomplete".equals(task.getStatus())) {
                return true;
            }
        }
        return false;
    }

    private void checkUpcomingDeadlines() {
        Calendar now = Calendar.getInstance();
        Calendar deadline = Calendar.getInstance();
        deadline.add(Calendar.HOUR, 48);
        
        boolean hasUpcomingTasks = false;
        
        for (Task task : allTasks) {
            if (task.getDueDate() != null && "incomplete".equals(task.getStatus())) {
                Calendar taskDate = Calendar.getInstance();
                taskDate.setTime(task.getDueDate());
                
                if (taskDate.after(now) && taskDate.before(deadline)) {
                    hasUpcomingTasks = true;
                    break;
                }
            }
        }

        if (hasUpcomingTasks) {
            triggerDeadlineNotification();
        }
    }

    private void triggerDeadlineNotification() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
        
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(1000);
            }
        }
        
        Toast.makeText(getContext(), "You have tasks due in the next 48 hours!", Toast.LENGTH_LONG).show();
    }

    private static class CalendarDay {
        Date date;
        boolean hasTasks;
        boolean hasUrgentTasks;
        List<Task> tasks;
        int dayNumber;

        CalendarDay(Date date, boolean hasTasks, boolean hasUrgentTasks, List<Task> tasks, int dayNumber) {
            this.date = date;
            this.hasTasks = hasTasks;
            this.hasUrgentTasks = hasUrgentTasks;
            this.tasks = tasks;
            this.dayNumber = dayNumber;
        }
    }

    private class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {
        private List<CalendarDay> days;

        CalendarAdapter(List<CalendarDay> days) {
            this.days = days;
        }

        @NonNull
        @Override
        public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_calendar_day, parent, false);
            return new CalendarViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
            CalendarDay day = days.get(position);
            holder.bind(day);
        }

        @Override
        public int getItemCount() {
            return days.size();
        }

        class CalendarViewHolder extends RecyclerView.ViewHolder {
            TextView dayNumber;
            View taskIndicator;
            View urgentIndicator;
            View dayLayout;

            CalendarViewHolder(@NonNull View itemView) {
                super(itemView);
                dayNumber = itemView.findViewById(R.id.dayNumber);
                taskIndicator = itemView.findViewById(R.id.taskIndicator);
                urgentIndicator = itemView.findViewById(R.id.urgentIndicator);
                dayLayout = itemView.findViewById(R.id.dayLayout);
            }

            void bind(CalendarDay day) {
                if (day.date == null) {
                    dayNumber.setText("");
                    taskIndicator.setVisibility(View.GONE);
                    urgentIndicator.setVisibility(View.GONE);
                    dayLayout.setBackgroundColor(Color.TRANSPARENT);
                    dayLayout.setOnClickListener(null);
                    return;
                }

                dayNumber.setText(String.valueOf(day.dayNumber));

                if (day.hasTasks) {
                    taskIndicator.setVisibility(View.VISIBLE);
                    if (day.hasUrgentTasks) {
                        urgentIndicator.setVisibility(View.VISIBLE);
                        taskIndicator.setBackgroundColor(Color.RED);
                    } else {
                        urgentIndicator.setVisibility(View.GONE);
                        taskIndicator.setBackgroundColor(Color.BLUE);
                    }
                } else {
                    taskIndicator.setVisibility(View.GONE);
                    urgentIndicator.setVisibility(View.GONE);
                }

                Calendar today = Calendar.getInstance();
                Calendar cellDate = Calendar.getInstance();
                cellDate.setTime(day.date);
                
                if (today.get(Calendar.YEAR) == cellDate.get(Calendar.YEAR) &&
                    today.get(Calendar.MONTH) == cellDate.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) == cellDate.get(Calendar.DAY_OF_MONTH)) {
                    dayLayout.setBackgroundColor(Color.parseColor("#E3F2FD"));
                } else {
                    dayLayout.setBackgroundColor(Color.TRANSPARENT);
                }

                dayLayout.setOnClickListener(v -> {
                    if (day.hasTasks) {
                        StringBuilder taskList = new StringBuilder();
                        for (Task task : day.tasks) {
                            taskList.append("• ").append(task.getTitle()).append("\n");
                        }
                        Toast.makeText(getContext(), 
                            "Tasks for this day:\n" + taskList.toString(), 
                            Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
