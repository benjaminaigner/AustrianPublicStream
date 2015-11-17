package systems.byteswap.publicstream;

import android.content.Context;
import android.content.Intent;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

//TODO: Mediaplayer unten
//TODO: Live -> button clicklistener + mediaplayer laden

public class MainActivity extends AppCompatActivity {
    Timer listTimer;
    ProgramExpandableAdapter adapter;
    private ArrayList<ORFParser.ORFProgram> programListToday;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus1;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus2;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus3;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus4;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus5;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus6;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus7;

    public static int REFETCH_LIST_INTERVAL_SECONDS = 300; //each 5min

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //TODO: übergangslösung, eigentlich Async Task...
        //Siehe: https://stackoverflow.com/questions/13136539/caused-by-android-os-networkonmainthreadexception
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //create expandable list view / set properties
        ExpandableListView expandableList = (ExpandableListView)(findViewById(R.id.expandableProgramList));
        expandableList.setDividerHeight(2);
        expandableList.setGroupIndicator(null);
        expandableList.setClickable(true);

        //Load the list initially...

        //Create calendar object (today)
        Calendar today = new GregorianCalendar();
        //create parser object
        ORFParser parser = new ORFParser();

        programListToday = parser.getProgramsForDay(today.getTime());
        today.add(Calendar.DAY_OF_MONTH,-1);
        programListTodayMinus1 = parser.getProgramsForDay(today.getTime());
        today.add(Calendar.DAY_OF_MONTH,-1);
        programListTodayMinus2 = parser.getProgramsForDay(today.getTime());
        today.add(Calendar.DAY_OF_MONTH,-1);
        programListTodayMinus3 = parser.getProgramsForDay(today.getTime());
        today.add(Calendar.DAY_OF_MONTH,-1);
        programListTodayMinus4 = parser.getProgramsForDay(today.getTime());
        today.add(Calendar.DAY_OF_MONTH,-1);
        programListTodayMinus5 = parser.getProgramsForDay(today.getTime());
        today.add(Calendar.DAY_OF_MONTH,-1);
        programListTodayMinus6 = parser.getProgramsForDay(today.getTime());
        today.add(Calendar.DAY_OF_MONTH,-1);
        programListTodayMinus7 = parser.getProgramsForDay(today.getTime());

        // Create the Adapter
        adapter = new ProgramExpandableAdapter(programListToday, programListTodayMinus1,
                programListTodayMinus2, programListTodayMinus3, programListTodayMinus4, programListTodayMinus5,
                programListTodayMinus6, programListTodayMinus7);

        adapter.setInflater((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), this);

        // Set the Adapter to expandableList
        expandableList.setAdapter(adapter);
        //TODO:
        //expandableList.setOnChildClickListener(this.programClickListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        listTimer = new Timer();

        //schedule a timer
        listTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }

        }, 0, REFETCH_LIST_INTERVAL_SECONDS*1000);
    }

    private void TimerMethod()
    {
        //Create calendar object (today)
        Calendar today = new GregorianCalendar();
        //create parser object
        ORFParser parser = new ORFParser();

        programListToday = parser.getProgramsForDay(today.getTime());
        today.add(Calendar.DAY_OF_MONTH,-1);
        programListTodayMinus1 = parser.getProgramsForDay(today.getTime());
        today.add(Calendar.DAY_OF_MONTH,-1);
        programListTodayMinus2 = parser.getProgramsForDay(today.getTime());
        today.add(Calendar.DAY_OF_MONTH,-1);
        programListTodayMinus3 = parser.getProgramsForDay(today.getTime());
        today.add(Calendar.DAY_OF_MONTH,-1);
        programListTodayMinus4 = parser.getProgramsForDay(today.getTime());
        today.add(Calendar.DAY_OF_MONTH,-1);
        programListTodayMinus5 = parser.getProgramsForDay(today.getTime());
        today.add(Calendar.DAY_OF_MONTH,-1);
        programListTodayMinus6 = parser.getProgramsForDay(today.getTime());
        today.add(Calendar.DAY_OF_MONTH,-1);
        programListTodayMinus7 = parser.getProgramsForDay(today.getTime());

        adapter.update(programListToday, programListTodayMinus1,
                programListTodayMinus2, programListTodayMinus3, programListTodayMinus4, programListTodayMinus5,
                programListTodayMinus6, programListTodayMinus7);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Stop the regular list update
        listTimer.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /**
     * @param item Menu item, given by Android
     * @return true, if no valid item was found
     *
     * Handling the menu for the main activity, by now:
     * -) SettingsActivity
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch(id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
