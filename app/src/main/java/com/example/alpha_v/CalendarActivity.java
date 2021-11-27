package com.example.alpha_v;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import android.provider.CalendarContract;

import java.util.Calendar;

public class CalendarActivity extends AppCompatActivity {
    EditText eventTitleET, inventorET, emailET;
    String eventTitle, inventorName;

    int selectedYear, selectedMonth, selectedDayOfMonth, selectedHour, selectedMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        eventTitleET = findViewById(R.id.eventTitleET);
        inventorET = findViewById(R.id.inventorET);
        emailET = findViewById(R.id.emailET);
    }

    public void createEvent(View view) {
    eventTitle = eventTitleET.getText().toString();
    inventorName = inventorET.getText().toString();

    Calendar calendar = Calendar.getInstance();
    final int year = calendar.get(Calendar.YEAR);
    final int month = calendar.get(Calendar.MONTH);
    final int day = calendar.get(Calendar.DAY_OF_MONTH);
    DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            month ++;

            selectedYear = year;
            selectedMonth = month;
            selectedDayOfMonth = dayOfMonth;

            openTimePicker();
        }
        }, year, month, day);

        dpd.show();
    }


    private void openTimePicker() {
        final Calendar cldr = Calendar.getInstance();
        int hour = cldr.get(Calendar.HOUR_OF_DAY);
        int minutes = cldr.get(Calendar.MINUTE);
        // time picker dialog
        TimePickerDialog picker = new TimePickerDialog(CalendarActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker tp, int hour, int minute) {
                        selectedHour = hour;
                        selectedMinute = minute;

                        sendToGoogleCalender();
                        eventTitleET.setText("");
                        inventorET.setText("");
                    }
                }, hour, minutes, true);
        picker.show();
    }

    private void sendToGoogleCalender() {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, eventTitle);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, inventorName);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "Local");
        intent.putExtra(CalendarContract.Events.DTSTART, selectedHour);
        intent.putExtra(Intent.EXTRA_EMAIL, emailET.getText().toString());

        // todo: whats about the minutes???? just hours????

        if (intent.resolveActivity(getPackageManager()) != null){
            Intent intent1 = Intent.createChooser(intent, "Open using");
            startActivity(intent1);
        } else {
            Toast.makeText(this, "No supported app", Toast.LENGTH_SHORT).show();
        }
    }
}