package systems.byteswap.publicstream;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

//TODO: Liste minimiert sich wenn update -> fixing... -> is
//TODO: wie is das mitn telefonieren? GET_NOISY_INTENT...

public class MainActivity extends AppCompatActivity {
    Timer listTimer;
    Timer programDataTimer;
    Timer seekTimer;
    ProgramExpandableAdapter adapter;
    ExpandableListView expandableList;
    final Handler handler = new Handler();
    MediaService mService;
    int currentTime;
    int currentDuration;


    private static ArrayList<ORFParser.ORFProgram> programListToday;
    private static ArrayList<ORFParser.ORFProgram> programListTodayMinus1;
    private static ArrayList<ORFParser.ORFProgram> programListTodayMinus2;
    private static ArrayList<ORFParser.ORFProgram> programListTodayMinus3;
    private static ArrayList<ORFParser.ORFProgram> programListTodayMinus4;
    private static ArrayList<ORFParser.ORFProgram> programListTodayMinus5;
    private static ArrayList<ORFParser.ORFProgram> programListTodayMinus6;
    private static ArrayList<ORFParser.ORFProgram> programListTodayMinus7;

    public static int REFETCH_LIST_INTERVAL_SECONDS = 300; //each 5min


    public boolean hasChanged = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = ((LocalBinder<MediaService>) service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            // As our service is in the same process, this should never be called
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //start the mediaplayer service
        Intent mMediaServiceIntent = new Intent(this, MediaService.class);
        bindService(mMediaServiceIntent, mConnection, Context.BIND_AUTO_CREATE);

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

        //add a click listener to the "Live" button
        Button buttonLive = (Button) findViewById(R.id.buttonLive);
        buttonLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.onCommand(MediaService.ACTION_LOAD,ORFParser.ORF_LIVE_URL,0);
                TextView text = (TextView)findViewById(R.id.textViewCurrentStream);
                text.setText("LIVE");
            }
        });

        //add a click listener to the "Play/Pause" button
        ImageButton buttonPlayPause = (ImageButton) findViewById(R.id.buttonPause);
        buttonPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.onCommand(MediaService.ACTION_PLAY_PAUSE, "",0);
            }
        });

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(1000);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Log.e("PUBLICSTREAM","Seek: " + progress);
                //TODO: exception beim resize/orientation change
                mService.onCommand(MediaService.ACTION_SETTIME, String.valueOf((float)seekBar.getProgress()/1000),0);
            }
        });

    }

    //listener for list items clicks...
    public void programClickListener(ORFParser.ORFProgram child) {
        TextView streamtext = (TextView)findViewById(R.id.textViewCurrentStream);
        streamtext.setText(child.title);
        mService.onCommand(MediaService.ACTION_LOAD, child.url, child.id);
    }

    @Override
    public void onResume() {
        super.onResume();

        listTimer = new Timer();
        programDataTimer = new Timer();
        seekTimer = new Timer();

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

        //schedule a timer for the time update
        seekTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if(mService != null) {

                            SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
                            int timeStamp = mService.getCurrentPosition();
                            String dateString;

                            switch(timeStamp) {
                                //stopped/preparing... -> time: 0
                                case -1:
                                    dateString = formatter.format(new Date(0));
                                    break;
                                //pause: no change...
                                case -2:
                                    dateString = formatter.format(new Date(currentTime));
                                    break;
                                //Playing...
                                default:
                                    dateString = formatter.format(new Date(timeStamp));
                                    currentTime = timeStamp;
                                    break;
                            }
                            dateString += "/";

                            timeStamp = mService.getDuration();
                            switch(timeStamp) {
                                case -1:
                                    dateString += "00:00";
                                    break;
                                case -2:
                                    dateString += formatter.format(new Date(currentDuration));
                                    break;
                                default:
                                    dateString += formatter.format(new Date(timeStamp));
                                    currentDuration = timeStamp;
                                    break;
                            }

                            SeekBar seekbar = (SeekBar) findViewById(R.id.seekBar);
                            if(!mService.isLive()) {
                                try {
                                    seekbar.setProgress((int) (((float) currentTime / (float) currentDuration) * 1000));
                                } catch (ArithmeticException e) {
                                    Log.d("PUBLICSTREAM","Progressbar: Div by 0");
                                }
                            } else {
                                seekbar.setProgress(0);
                            }

                            TextView time = (TextView)findViewById(R.id.textViewTime);
                            time.setText(dateString);
                        }
                    }
                });
            }

        }, 0, 1000);
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
        seekTimer.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
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

