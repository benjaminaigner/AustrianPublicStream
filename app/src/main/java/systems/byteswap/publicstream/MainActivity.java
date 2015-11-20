package systems.byteswap.publicstream;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;



public class MainActivity extends AppCompatActivity {
    Timer listTimer;
    Timer programDataTimer;
    ProgramExpandableAdapter adapter;
    ExpandableListView expandableList;
    final Handler handler = new Handler();
    private MediaService mBoundService;


    private ArrayList<ORFParser.ORFProgram> programListToday;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus1;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus2;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus3;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus4;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus5;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus6;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus7;

    public static int REFETCH_LIST_INTERVAL_SECONDS = 300; //each 5min

    public boolean hasChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //start the mediaplayer service
        startService(new Intent(this, MainActivity.class));

        //create expandable list view / set properties
        expandableList = (ExpandableListView)(findViewById(R.id.expandableProgramList));
        expandableList.setDividerHeight(2);
        expandableList.setGroupIndicator(null);
        expandableList.setClickable(true);

        // Create the Adapter
        adapter = new ProgramExpandableAdapter();

        adapter.setInflater((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), this);

        // Set the Adapter to expandableList
        expandableList.setAdapter(adapter);

        expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Log.e("PUBLICSTREAM", "Should load ondemand stream...");
                //TODO: mediaplayer -> load ondemand...
                return false;
            }
        });

        //add a click listener to the "Live" button
        Button buttonLive = (Button) findViewById(R.id.buttonLive);
        buttonLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("PUBLICSTREAM", "Should load live stream...");
                //TODO: mediaplayer -> load live...
            }
        });

        //add a click listener to the "Play/Pause" button
        ImageButton buttonPlayPause = (ImageButton) findViewById(R.id.buttonPause);
        buttonPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("PUBLICSTREAM", "Should play/pause the current stream...");
                //TODO: mediaplayer -> play/pause...
            }
        });

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.e("PUBLICSTREAM", "Should seek current stream: " + progress);
                //TODO: mediaplayer -> seek
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //TODO: entfernen und oben einfügen...
    public void programClickListener(ORFParser.ORFProgram child) {
        TextView streamtext = (TextView)findViewById(R.id.textViewCurrentStream);
        streamtext.setText(child.title);
        Log.e("PUBLICSTREAM","Should start a stream...");
    }

    @Override
    public void onResume() {
        super.onResume();

        listTimer = new Timer();
        programDataTimer = new Timer();

        programDataTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //Create calendar object (today)
                Calendar today = new GregorianCalendar();
                //create parser object
                ORFParser parser = new ORFParser();

                ArrayList<ORFParser.ORFProgram> temp;

                temp = parser.getProgramsForDay(today.getTime());
                if(!temp.equals(programListToday)) {
                    programListToday = temp;
                    hasChanged = true;
                }

                today.add(Calendar.DAY_OF_MONTH,-1);
                temp = parser.getProgramsForDay(today.getTime());
                if(!temp.equals(programListTodayMinus1)) {
                    programListTodayMinus1 = temp;
                    hasChanged = true;
                }

                today.add(Calendar.DAY_OF_MONTH,-1);
                temp = parser.getProgramsForDay(today.getTime());
                if(!temp.equals(programListTodayMinus2)) {
                    programListTodayMinus2 = temp;
                    hasChanged = true;
                }
                today.add(Calendar.DAY_OF_MONTH,-1);
                temp = parser.getProgramsForDay(today.getTime());
                if(!temp.equals(programListTodayMinus3)) {
                    programListTodayMinus3 = temp;
                    hasChanged = true;
                }
                today.add(Calendar.DAY_OF_MONTH,-1);
                temp = parser.getProgramsForDay(today.getTime());
                if(!temp.equals(programListTodayMinus4)) {
                    programListTodayMinus4 = temp;
                    hasChanged = true;
                }
                today.add(Calendar.DAY_OF_MONTH,-1);
                temp = parser.getProgramsForDay(today.getTime());
                if(!temp.equals(programListTodayMinus5)) {
                    programListTodayMinus5 = temp;
                    hasChanged = true;
                }
                today.add(Calendar.DAY_OF_MONTH,-1);
                temp = parser.getProgramsForDay(today.getTime());
                if(!temp.equals(programListTodayMinus6)) {
                    programListTodayMinus6 = temp;
                    hasChanged = true;
                }
                today.add(Calendar.DAY_OF_MONTH,-1);
                temp = parser.getProgramsForDay(today.getTime());
                if(!temp.equals(programListTodayMinus7)) {
                    programListTodayMinus7 = temp;
                    hasChanged = true;
                }

            }
        }, 0, REFETCH_LIST_INTERVAL_SECONDS * 1000);

        //schedule a timer
        listTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        TimerMethod();
                    }
                });
            }

        }, 0, 2000);
    }



    /** update the list view, the data is fetched in the other timer method */
    private void TimerMethod() {
        adapter.update(programListToday, programListTodayMinus1,
                programListTodayMinus2, programListTodayMinus3, programListTodayMinus4, programListTodayMinus5,
                programListTodayMinus6, programListTodayMinus7);
        if(expandableList != null && adapter != null && hasChanged) {
            expandableList.setAdapter(adapter);
            hasChanged = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Stop the regular list update
        listTimer.cancel();
        programDataTimer.cancel();
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
