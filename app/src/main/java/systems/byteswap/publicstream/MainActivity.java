package systems.byteswap.publicstream;

import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
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
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

//TODO: wie is das mitn telefonieren? GET_NOISY_INTENT...
//TODO: löschen button einbauen für offline sachen (minor, löschen geht auch im filemanager)

public class MainActivity extends AppCompatActivity {
    /** number of seconds between each list fetching action (getting all JSON files of programs, takes about 2-5s) **/
    public static int REFETCH_LIST_INTERVAL_SECONDS = 300; //each 5min

    /** ID for the download notification, unique to differ the notifications for the update **/
    public static int NOTIFICATION_DOWNLOAD_ID = 1;
    /** ID for the play notification, unique to differ the notifications for the update **/
    public static int NOTIFICATION_PLAY_ID = 2;


    Timer listTimer;
    Timer programDataTimer;
    Timer seekTimer;
    ProgramExpandableAdapter adapter;
    ExpandableListView expandableList;
    final Handler handler = new Handler();
    MediaService mService;
    int currentTime;
    int currentDuration;
    private MainFragment dataFragment;
    private ArrayList<ORFParser.ORFProgram> programListToday;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus1;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus2;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus3;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus4;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus5;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus6;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus7;
    private ArrayList<ORFParser.ORFProgram> programListOffline;
    boolean isPausedNotified = false;

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

        // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        dataFragment = (MainFragment) fm.findFragmentByTag("data");
        // create the fragment and data the first time
        // or load existing data...
        if (dataFragment == null) {
            // add the fragment
            dataFragment = new MainFragment();
            fm.beginTransaction().add(dataFragment, "data").commit();
        } else {
            programListToday = dataFragment.getProgramListToday();
            programListTodayMinus1 = dataFragment.getProgramListTodayMinus1();
            programListTodayMinus2 = dataFragment.getProgramListTodayMinus2();
            programListTodayMinus3 = dataFragment.getProgramListTodayMinus3();
            programListTodayMinus4 = dataFragment.getProgramListTodayMinus4();
            programListTodayMinus5 = dataFragment.getProgramListTodayMinus5();
            programListTodayMinus6 = dataFragment.getProgramListTodayMinus6();
            programListTodayMinus7 = dataFragment.getProgramListTodayMinus7();
            programListOffline = dataFragment.getProgramListOffline();
        }


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
                mService.onCommand(MediaService.ACTION_LOAD,ORFParser.ORF_LIVE_URL);
                TextView text = (TextView)findViewById(R.id.textViewCurrentStream);
                text.setText("LIVE");
            }
        });

        //add a click listener to the "Play/Pause" button
        ImageButton buttonPlayPause = (ImageButton) findViewById(R.id.buttonPause);
        buttonPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.onCommand(MediaService.ACTION_PLAY_PAUSE, "");
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
                if(mService != null) {
                    mService.onCommand(MediaService.ACTION_SETTIME, String.valueOf((float)seekBar.getProgress()/1000));
                }
            }
        });

    }

    //listener for list items clicks...
    public void programClickListener(ORFParser.ORFProgram child) {
        TextView streamtext = (TextView)findViewById(R.id.textViewCurrentStream);
        streamtext.setText(child.title);
        Toast.makeText(MainActivity.this, "Play", Toast.LENGTH_SHORT).show();
        mService.onCommand(MediaService.ACTION_LOAD, child.url);
        //mService.onCommand(MediaService.ACTION_LOAD, child.url, child.id);
    }



    //listener for download item clicks
    //Download according to: https://stackoverflow.com/questions/6407324/how-to-get-image-from-url-in-android/13174188#13174188
    public void programDownloadClickListener(final ORFParser.ORFProgram child, final String datum) {
        Toast.makeText(MainActivity.this, "Download...", Toast.LENGTH_SHORT).show();
        //String message;
        new Thread(new Runnable() {
            public void run() {
                String fileName;
                try{
                    ORFParser parser = new ORFParser();
                    //setup the connection
                    URL orfURL = new URL(child.url);
                    HttpURLConnection urlConnection = (HttpURLConnection) orfURL.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(true);
                    urlConnection.connect();

                    //create the file
                    File folder = new File(Environment.getExternalStorageDirectory().toString() + "/Ö1-Beiträge");
                    //String fileName = datum + "-" + child.time + "-" + child.shortTitle + ".mp3";
                    fileName = datum + "-" + child.time.replace(':','.') + "-" + child.shortTitle + ".mp3";
                    folder.mkdirs();

                    File file = new File(folder, fileName);
                    if (file.createNewFile()) {
                        file.createNewFile();
                    }

                    //Setup the streams
                    FileOutputStream fileOutput = new FileOutputStream(file);
                    InputStream inputStream = urlConnection.getInputStream();

                    //this is the total size of the file
                    int totalSize = urlConnection.getContentLength();
                    //variable to store total downloaded bytes
                    int downloadedSize = 0;

                    //create a buffer...
                    byte[] buffer = new byte[1024];
                    int bufferLength; //used to store a temporary size of the buffer

                    //Create a notification to show the download progress
                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    int progress = 0;
                    int progresstemp;
                    NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(getBaseContext())
                            .setContentTitle("Download")
                            .setContentText(child.title + "0%")
                            .setSmallIcon(R.drawable.notification_download);





                    //now, read through the input buffer and write the contents to the file
                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        //add the data in the buffer to the file in the file output stream (the file on the sd card
                        fileOutput.write(buffer, 0, bufferLength);
                        //add up the size so we know how much is downloaded
                        downloadedSize += bufferLength;
                        //show every progress change in the notification
                        progresstemp = (int)(((float)downloadedSize/(float)totalSize)*100);
                        if(progresstemp != progress) {
                            mNotifyBuilder.setContentText(child.title + " " + progresstemp +"%");
                            mNotifyBuilder.setProgress(100, progresstemp, false);
                            mNotificationManager.notify(
                                    MainActivity.NOTIFICATION_DOWNLOAD_ID,
                                    mNotifyBuilder.build());
                            progress = progresstemp;
                        }
                    }
                    //close the output stream when done

                    fileOutput.close();

                    if(downloadedSize==totalSize) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getBaseContext(), "Download abgeschlossen", Toast.LENGTH_SHORT).show();
                            }
                        });
                        //Finally: add the downloaded program to the offline list and update the UI...
                        child.url = folder + "/" + fileName;
                        parser.addProgramOffline(child,getBaseContext().getExternalCacheDir());
                        programListOffline = parser.getProgramsOffline(getBaseContext().getExternalCacheDir());
                        hasChanged = true;

                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getBaseContext(), "Fehler: unvollständiger Download", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch(final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getBaseContext(), "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
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
                boolean hasChangedTemp = false;
                //Create calendar object (today)
                Calendar today = new GregorianCalendar();
                //create parser object
                ORFParser parser = new ORFParser();

                ArrayList<ORFParser.ORFProgram> temp;

                temp = parser.getProgramsForDay(today.getTime());
                if(!temp.equals(programListToday)) {
                    programListToday = temp;
                    dataFragment.setProgramListToday(temp);
                    hasChangedTemp = true;
                }

                today.add(Calendar.DAY_OF_MONTH,-1);
                temp = parser.getProgramsForDay(today.getTime());
                if(!temp.equals(programListTodayMinus1)) {
                    programListTodayMinus1 = temp;
                    dataFragment.setProgramListTodayMinus1(temp);
                    hasChangedTemp = true;
                }

                today.add(Calendar.DAY_OF_MONTH,-1);
                temp = parser.getProgramsForDay(today.getTime());
                if(!temp.equals(programListTodayMinus2)) {
                    programListTodayMinus2 = temp;
                    dataFragment.setProgramListTodayMinus2(temp);
                    hasChangedTemp = true;
                }
                today.add(Calendar.DAY_OF_MONTH,-1);
                temp = parser.getProgramsForDay(today.getTime());
                if(!temp.equals(programListTodayMinus3)) {
                    programListTodayMinus3 = temp;
                    dataFragment.setProgramListTodayMinus3(temp);
                    hasChangedTemp = true;
                }
                today.add(Calendar.DAY_OF_MONTH,-1);
                temp = parser.getProgramsForDay(today.getTime());
                if(!temp.equals(programListTodayMinus4)) {
                    programListTodayMinus4 = temp;
                    dataFragment.setProgramListTodayMinus4(temp);
                    hasChangedTemp = true;
                }
                today.add(Calendar.DAY_OF_MONTH,-1);
                temp = parser.getProgramsForDay(today.getTime());
                if(!temp.equals(programListTodayMinus5)) {
                    programListTodayMinus5 = temp;
                    dataFragment.setProgramListTodayMinus5(temp);
                    hasChangedTemp = true;
                }
                today.add(Calendar.DAY_OF_MONTH,-1);
                temp = parser.getProgramsForDay(today.getTime());
                if(!temp.equals(programListTodayMinus6)) {
                    programListTodayMinus6 = temp;
                    dataFragment.setProgramListTodayMinus6(temp);
                    hasChangedTemp = true;
                }
                today.add(Calendar.DAY_OF_MONTH,-1);
                temp = parser.getProgramsForDay(today.getTime());
                if(!temp.equals(programListTodayMinus7)) {
                    programListTodayMinus7 = temp;
                    dataFragment.setProgramListTodayMinus7(temp);
                    hasChangedTemp = true;
                }

                temp = parser.getProgramsOffline(getBaseContext().getExternalCacheDir());
                if(temp != null) {
                    if (!temp.equals(programListOffline)) {
                        programListOffline = temp;
                        dataFragment.setProgramListOffline(temp);
                        hasChangedTemp = true;
                    }
                }

                //if one list object is changed -> set global change flag
                hasChanged = hasChangedTemp;
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

                    Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                            //.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

                    NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(getBaseContext())
                            .setContentTitle("Ö1 - PublicStream")
                            .setSmallIcon(R.drawable.notification_play).setContentIntent(contentIntent);
                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    //mNotifyBuilder.setLatestEventInfo(getApplicationContext(), from, message, contentIntent);


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
                            //add the separator
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

                            //set the corresponding notification
                            switch(timeStamp) {
                                case -1:
                                    mNotificationManager.cancel(MainActivity.NOTIFICATION_PLAY_ID);
                                    isPausedNotified = false;
                                    break;
                                case -2:
                                    if(!isPausedNotified) {
                                        isPausedNotified = true;
                                        mNotifyBuilder.setContentText("Pause: " + dateString);
                                        mNotificationManager.notify(
                                                MainActivity.NOTIFICATION_PLAY_ID,
                                                mNotifyBuilder.build());
                                    }
                                    break;
                                default:
                                    //wenn nicht mehr aktiv -> pausieren...
                                    mNotifyBuilder.setContentText("Abspielen: " + dateString);
                                    mNotificationManager.notify(
                                            MainActivity.NOTIFICATION_PLAY_ID,
                                            mNotifyBuilder.build());
                                    isPausedNotified = false;
                                    break;
                            }

                            //Update the time in the text view (GUI, bottom right)
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
                programListTodayMinus2, programListTodayMinus3,
                programListTodayMinus4, programListTodayMinus5,
                programListTodayMinus6, programListTodayMinus7,
                programListOffline);
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
        //TODO: braucht das viel Akku wenn der Timer immer läuft?
        //seekTimer.cancel();
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

