/**
        Copyright:
        2015/2016 Benjamin Aigner

        This file is part of AustrianPublicStream.

        AustrianPublicStream is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        AustrianPublicStream is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with AustrianPublicStream.  If not, see <http://www.gnu.org/licenses/>.


        Contribution:
        A big "thank you" to following projects/webpages providing sourcecode/libraries/information:

        ToxicBakery for its transformer library (https://github.com/ToxicBakery/ViewPagerTransforms)
        VideoLAN for its VLC library (https://wiki.videolan.org/Libvlc/)
        RomanNurik for the Android Asset Studio (https://romannurik.github.io/AndroidAssetStudio/)

        and of course all the experts on stackoverflow for helpful hints!
**/

package systems.byteswap.publicstream;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ToxicBakery.viewpager.transforms.CubeOutTransformer;

import org.videolan.libvlc.util.AndroidUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

//TODO: ordentlich kommentieren & clean-up

public class MainActivity extends AppCompatActivity {
    /** debug tags **/
    public static String TAG_REMOTELIST = "REMOTELIST";
    public static String TAG_ADAPTER = "ADAPTERPROGS";

    /** ID for the download notification, unique to differ the notifications for the update
     * Note: this number should be the highest amongst all notification, because it will be
     * increased for each concurrent download!!! **/
    public static int NOTIFICATION_DOWNLOAD_ID = 2;
    /** ID for the play notification, unique to differ the notifications for the update **/
    public static int NOTIFICATION_PLAY_ID = 1;

    //Handler to process all postDelayed operations (timer replacement for Android)
    private Handler handler = new Handler();

    MediaService mService;
    int currentTime;
    int currentDuration;

    static ProgramExpandableAdapter adapter;
    static HeaderAdapter headerAdapter;

    boolean useHWAccel = true;

    //Timer instance for the remotelist (not working with handler -> NetworkOnMainThread exception)
    Timer programDataTimer;

    //Runnable instance for the seek update: time in the main activity & notification (will be posted to the handler)
    Runnable mRunnableSeek = new Runnable() {
        public void run() {
            TimerMethodSeek();
        }
    };

    /**
     The {@link android.support.v4.view.PagerAdapter} that will provide
     fragments for each of the sections. We use a
     {@link FragmentPagerAdapter} derivative, which will keep every
     loaded fragment in memory. If this becomes too memory intensive, it
     may be best to switch to a
     {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    private MainFragment dataFragment;
    private static ArrayList<ORFParser.ORFProgram> programListToday;
    private static ArrayList<ORFParser.ORFProgram> programListTodayMinus1;
    private static ArrayList<ORFParser.ORFProgram> programListTodayMinus2;
    private static ArrayList<ORFParser.ORFProgram> programListTodayMinus3;
    private static ArrayList<ORFParser.ORFProgram> programListTodayMinus4;
    private static ArrayList<ORFParser.ORFProgram> programListTodayMinus5;
    private static ArrayList<ORFParser.ORFProgram> programListTodayMinus6;
    private static ArrayList<ORFParser.ORFProgram> programListTodayMinus7;
    private static ArrayList<ORFParser.ORFProgram> programListOffline;
    /** boolean flag to show if a notification was already created. If paused, only one notification is issued */
    boolean isPausedNotified = false;

    /** flag (from the settings) to show if a notification is issued during playback */
    boolean showPausedNotification = true;

    /** flag (from the settings) to show if a notification is issued if paused */
    boolean showPlayNotification = true;

    /** flag (from the settings) to show if a notification is issued on the lockscreen */
    boolean showLockscreenNotification = true;

    private ServiceConnection mConnection;

    int currentDownloadNotificationId = NOTIFICATION_DOWNLOAD_ID;

    public final static int NOTIFICATION_COMMAND_CANCEL = 1;
    public final static int NOTIFICATION_COMMAND_UPDATE_TEXT = 2;
    public final static int NOTIFICATION_COMMAND_UPDATE_ICON = 3;
    public final static int NOTIFICATION_COMMAND_NOTHING = 4;

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        FragmentManager fm = getFragmentManager();
        dataFragment = (MainFragment) fm.findFragmentByTag("data");
        if(dataFragment == null) {
            createWithoutFragment();
        } else {
            restoreFromFragment();
        }
    }

    void createWithoutFragment() {
        Intent mMediaServiceIntent;

        // add the fragment
        dataFragment = new MainFragment();


        mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                //mService = ((LocalBinder<MediaService>) service).getService();
                mService = MediaService.getService(service);
                dataFragment.setMediaService(mService);
            }

            public void onServiceDisconnected(ComponentName className) {
            }
        };

        dataFragment.setMediaConnection(mConnection);

        //start the mediaplayer service
        mMediaServiceIntent = new Intent(this, MediaService.class);
        startService(mMediaServiceIntent);
        bindService(mMediaServiceIntent,mConnection,Context.BIND_IMPORTANT);
        dataFragment.setMediaServiceIntent(mMediaServiceIntent);
    }


    void restoreFromFragment() {
        if(dataFragment == null) return;

        Intent mMediaServiceIntent;

        //Restore everything necessary from the dataFragment (if available)
        programListToday = dataFragment.getProgramListToday();
        programListTodayMinus1 = dataFragment.getProgramListTodayMinus1();
        programListTodayMinus2 = dataFragment.getProgramListTodayMinus2();
        programListTodayMinus3 = dataFragment.getProgramListTodayMinus3();
        programListTodayMinus4 = dataFragment.getProgramListTodayMinus4();
        programListTodayMinus5 = dataFragment.getProgramListTodayMinus5();
        programListTodayMinus6 = dataFragment.getProgramListTodayMinus6();
        programListTodayMinus7 = dataFragment.getProgramListTodayMinus7();
        currentDuration = dataFragment.getCurrentDuration();
        currentTime = dataFragment.getCurrentTime();
        programListOffline = dataFragment.getProgramListOffline();
        mService = dataFragment.getMediaService();
        mConnection = dataFragment.getMediaConnection();
        mMediaServiceIntent = dataFragment.getMediaServiceIntent();
        currentDownloadNotificationId = dataFragment.getCurrentDownloadNotificationId();

        //if something went wrong, restart the connection...
        if(mMediaServiceIntent == null || mConnection == null || mService == null) {
            Log.w("Restore activity","Something went wrong with the service connection, restarting: ");
            Log.w("Restore activity", "mMediaServiceIntent:" + mMediaServiceIntent);
            Log.w("Restore activity", "mConnection:" + mConnection);
            Log.w("Restore activity", "mService:" + mService);
            mMediaServiceIntent = new Intent(this, MediaService.class);
            startService(mMediaServiceIntent);

            mConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName className, IBinder service) {
                    //mService = ((LocalBinder<MediaService>) service).getService();
                    mService = MediaService.getService(service);
                    dataFragment.setMediaService(mService);
                }

                public void onServiceDisconnected(ComponentName className) {
                    // As our service is in the same process, this should never be called
                }
            };
            dataFragment.setMediaConnection(mConnection);
            dataFragment.setMediaServiceIntent(mMediaServiceIntent);
        }

        bindService(mMediaServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        TextView textViewCurrentStream = (TextView)findViewById(R.id.textViewCurrentStream);
        if(dataFragment.getTextPlayButton() != null) textViewCurrentStream.setText(dataFragment.getTextPlayButton());
    }

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
            //create everything necessary new
            createWithoutFragment();
            //commit our fragment (containing data) to the FragmentManager
            fm.beginTransaction().add(dataFragment, "data").commit();
        } else {
            //Existing fragment -> restore everything from there
            restoreFromFragment();
        }

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());



        IntentFilter filter = new IntentFilter();
        filter.setPriority(Integer.MAX_VALUE);
        filter.addAction(MediaService.ACTION_PLAY_PAUSE);
        registerReceiver(mReceiver, filter);
        registerV21();

        /**
            Set up the ViewPager with the sections adapter.
            The {@link ViewPager} that will host the section contents.
        */
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(true, new CubeOutTransformer());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        addGUIListener();
    }

    /** add all callbacks to the corresponding GUI elements:
     * -) Play the live stream
     * -) Play/Pause button
     * -) Seekbar
     */
    public void addGUIListener() {
        //add a click listener to the "Live" button
        Button buttonLive = (Button) findViewById(R.id.buttonLive);
        buttonLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.setUseHWAcceleration(useHWAccel);
                mService.onCommand(MediaService.ACTION_LOAD, ORFParser.ORF_LIVE_URL);
                TextView text = (TextView) findViewById(R.id.textViewCurrentStream);
                if (dataFragment != null) dataFragment.setTextPlayButton("LIVE");
                text.setText("LIVE");
                handler.removeCallbacks(mRunnableSeek);
                handler.postDelayed(mRunnableSeek, 1000);
                updateGUIElementsVisibility();
            }
        });

        //add a click listener to the "Play/Pause" button
        final ImageButton buttonPlayPause = (ImageButton) findViewById(R.id.buttonPause);
        buttonPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mService != null) {
                    mService.setUseHWAcceleration(useHWAccel);
                    mService.onCommand(MediaService.ACTION_PLAY_PAUSE, "");
                }
                handler.removeCallbacks(mRunnableSeek);
                handler.postDelayed(mRunnableSeek, 1000);
                updateGUIElementsVisibility();
            }
        });
        updateGUIElementsVisibility();

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
                if (mService != null) {
                    mService.onCommand(MediaService.ACTION_SETTIME, String.valueOf((float) seekBar.getProgress() / 1000));
                    updateGUIElementsVisibility();
                }
            }
        });
    }

    /**
     * Update the visibility settings for the play/pause button in the main activity
     */
    private void updateGUIElementsVisibility() {
        ImageButton buttonPlayPause = (ImageButton) findViewById(R.id.buttonPause);
        TextView time = (TextView)findViewById(R.id.textViewTime);
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        buttonPlayPause.setImageResource(R.drawable.ic_av_pause_circle_outline);
        if(mService != null) {
            switch(mService.getState()) {
                case MediaService.MEDIA_STATE_PLAYING:
                    seekBar.setVisibility(View.VISIBLE);
                    time.setVisibility(View.VISIBLE);
                    buttonPlayPause.setVisibility(View.VISIBLE);
                    break;
                case MediaService.MEDIA_STATE_IDLE:
                    seekBar.setVisibility(View.INVISIBLE);
                    time.setVisibility(View.INVISIBLE);
                    buttonPlayPause.setVisibility(View.INVISIBLE);
                    break;
                case MediaService.MEDIA_STATE_PAUSED:
                    seekBar.setVisibility(View.VISIBLE);
                    time.setVisibility(View.VISIBLE);
                    buttonPlayPause.setVisibility(View.VISIBLE);
                    buttonPlayPause.setImageResource(R.drawable.ic_av_play_circle_outline);
                    break;
                default:
                    break;
            }
        } else {
            seekBar.setVisibility(View.INVISIBLE);
            time.setVisibility(View.INVISIBLE);
            buttonPlayPause.setVisibility(View.INVISIBLE);
        }
    }

    /** callback listener for short clicks
     * Used to play the selected programs
     */
    public void programClickListener(ORFParser.ORFProgram child) {
        TextView streamtext = (TextView) findViewById(R.id.textViewCurrentStream);
        streamtext.setText(child.title);
        if(dataFragment != null) dataFragment.setTextPlayButton(child.title);
        Toast.makeText(MainActivity.this, "Play", Toast.LENGTH_SHORT).show();
        mService.setUseHWAcceleration(useHWAccel);
        mService.onCommand(MediaService.ACTION_LOAD, child.url);
        updateGUIElementsVisibility();
        //schedule the perdiodic seek time / notification update
        handler.removeCallbacks(mRunnableSeek);
        handler.postDelayed(mRunnableSeek, 1000);
    }

    /** callback listener for long clicks.
     * Used to delete an already loaded program
     * or download a remote one
    */
    public void programLongClickListener(final ORFParser.ORFProgram child, boolean toDelete, String datum) {
        if(toDelete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Wollen Sie den Beitrag " + child.shortTitle + " wirklich löschen?")
                    .setTitle("Löschen");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //delete the list entry & update the list
                    ORFParser parser = new ORFParser();

                    parser.removeProgramOffline(child, getBaseContext().getExternalCacheDir());
                    Toast.makeText(MainActivity.this, "Beitrag gelöscht", Toast.LENGTH_SHORT).show();
                    //Update the list
                    ArrayList<ORFParser.ORFProgram> temp = parser.getProgramsOffline(getBaseContext().getExternalCacheDir());
                    if(temp != null) {
                        programListOffline = temp;
                        dataFragment.setProgramListOffline(temp);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
                            mViewPager.getAdapter().notifyDataSetChanged();
                        }
                    });
                }
            });
            builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });

            builder.create().show();

        } else {
            programDownloadClickListener(child, datum);
        }
    }


    private class DownloadProgramTask extends AsyncTask<ORFParser.ORFProgram, Integer, Void> {
        int notificationId = NOTIFICATION_DOWNLOAD_ID;

        public DownloadProgramTask(int notificationId) {
            this.notificationId = notificationId;
        }

        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
        //Download according to: https://stackoverflow.com/questions/6407324/how-to-get-image-from-url-in-android/13174188#13174188
        protected Void doInBackground(ORFParser.ORFProgram... childs) {
            ORFParser.ORFProgram child = childs[0];
            String fileName;
            try {
                ORFParser parser = new ORFParser();
                //setup the connection
                URL orfURL = new URL(child.url);
                HttpURLConnection urlConnection = (HttpURLConnection) orfURL.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.connect();

                //create the file
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                //TODO: external storage? is jetzt nicht die SD Karte...
                String settingsPath = settings.getString(getString(R.string.SETTINGS_DOWNLOADFOLDER), Environment.getExternalStorageDirectory().toString());
                if (settingsPath.equals(""))
                    settingsPath = Environment.getExternalStorageDirectory().toString();

                File folder = new File(settingsPath + "/Ö1-Beiträge");
                fileName = child.dayLabel + "-" + child.time.replace(':', '.') + "-" + child.shortTitle.replace('/','_') + ".mp3";
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
                    progresstemp = (int) (((float) downloadedSize / (float) totalSize) * 100);
                    if (progresstemp != progress) {
                        mNotifyBuilder.setContentText(child.title + " " + progresstemp + "%");
                        mNotifyBuilder.setProgress(100, progresstemp, false);
                        mNotificationManager.notify(
                                this.notificationId,
                                mNotifyBuilder.build());
                        progress = progresstemp;
                    }

                    if (isCancelled()) {
                        mNotificationManager.cancelAll();
                        break;
                    }
                }
                //close the output stream when done

                fileOutput.close();

                if (downloadedSize == totalSize) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getBaseContext(), "Download abgeschlossen", Toast.LENGTH_SHORT).show();
                        }
                    });
                    //Finally: add the downloaded program to the offline list and update the UI...
                    child.url = folder + "/" + fileName;
                    parser.addProgramOffline(child, getBaseContext().getExternalCacheDir());
                    programListOffline = parser.getProgramsOffline(getBaseContext().getExternalCacheDir());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
                                mViewPager.getAdapter().notifyDataSetChanged();
                            } catch (IllegalStateException e) {
                                Log.w("DOWNLOAD",e.getMessage());
                            }
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getBaseContext(), "Fehler: unvollständiger Download", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch(final IOException e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.e("DOWNLOAD", e.getMessage());
                        Toast.makeText(getBaseContext(), "Fehler: keine Berechtigung (bitte in den Einstellungen freischalten)", Toast.LENGTH_LONG).show();
                    }
                });
            } catch(final Exception e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.e("DOWNLOAD", e.getMessage());
                        Toast.makeText(getBaseContext(), "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
        }


        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        protected void onPostExecute(Void result) {

        }
    }


    /** listener for download item clicks **/
    public void programDownloadClickListener(final ORFParser.ORFProgram child, final String datum) {
        Toast.makeText(getBaseContext(), "Download: " + child.shortTitle, Toast.LENGTH_SHORT).show();
        //increment the download notification id for each download (it is reset if the app is completely closed)
        currentDownloadNotificationId++;
        //store the current id to the fragment
        dataFragment.setCurrentDownloadNotificationId(currentDownloadNotificationId);
        //create a new AsyncTask
        new DownloadProgramTask(currentDownloadNotificationId).execute(child);
    }

    @Override
    public void onResume() {
        super.onResume();
        //load settings from preferences (interval of the refetch and notification settings)
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int interval = Integer.valueOf(settings.getString(getString(R.string.SETTINGS_REFETCH_INTERVAL), "5"));
        showPausedNotification = settings.getBoolean(getString(R.string.SETTINGS_SHOW_PAUSED_NOTIFICATION),true);
        showPlayNotification = settings.getBoolean(getString(R.string.SETTINGS_SHOW_PLAY_NOTIFICATION),true);
        showLockscreenNotification = settings.getBoolean(getString(R.string.SETTINGS_SHOW_LOCKSCREEN_NOTIFICATION),true);
        useHWAccel = settings.getBoolean(getString(R.string.SETTINGS_HW_ACCELERATOR),true);

        //schedule the regular update of the remote list
        programDataTimer = new Timer();
        programDataTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethodRemoteList();
            }
        }, 0, interval * 60 * 1000);


        //Create the regular update timer for the notifications and the progress bar in the GUI
        attachTimerMethodSeek();
        //Update the Play/Pause button in the main activity
        updateGUIElementsVisibility();
    }

    public void attachTimerMethodSeek() {
        handler.removeCallbacks(mRunnableSeek);
        handler.post(mRunnableSeek);
    }

    /**
     * Regular called timer method, fetching the remote lists in 9 concurrent threads
     */
    private void TimerMethodRemoteList() {
        //fetch all offline programs first

        ArrayList<ORFParser.ORFProgram> temp;
        ORFParser parser = new ORFParser();
        temp = parser.getProgramsOffline(getBaseContext().getExternalCacheDir());
        if(temp != null) {
            if (!temp.equals(programListOffline)) {
                programListOffline = temp;
                try {
                    dataFragment.setProgramListOffline(temp);
                } catch (Exception e) {
                    Log.e(MainActivity.TAG_REMOTELIST,"Exception while saving list to fragment");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
                        try {
                            mViewPager.getAdapter().notifyDataSetChanged();
                        } catch (IllegalStateException e) {
                            Log.w(MainActivity.TAG_REMOTELIST,"Exception while updating list: " + e.getMessage());
                        }
                    }
                });
            }
        }


        //post all fetch actions (for each day since today-1week)
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Create calendar object (today)
                Calendar today = new GregorianCalendar();
                //create parser object
                ORFParser parser = new ORFParser();

                ArrayList<ORFParser.ORFProgram> temp;
                temp = parser.getProgramsForDay(today.getTime());
                if(temp != null && !temp.equals(programListToday)) {
                    programListToday = temp;
                    try {
                        dataFragment.setProgramListToday(temp);
                    } catch (Exception e) {
                        Log.e(MainActivity.TAG_REMOTELIST,"Exception while saving list to fragment");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
                            try {
                                mViewPager.getAdapter().notifyDataSetChanged();
                            } catch (IllegalStateException e) {
                                Log.w(MainActivity.TAG_REMOTELIST,"Exception while updating list: " + e.getMessage());
                            }
                        }
                    });
                }
            }
        }).start();


        new Thread(new Runnable() {
            @Override
            public void run() {
                //Create calendar object (today)
                Calendar today = new GregorianCalendar();
                //create parser object
                ORFParser parser = new ORFParser();

                ArrayList<ORFParser.ORFProgram> temp;

                today.add(Calendar.DAY_OF_MONTH,-1);
                temp = parser.getProgramsForDay(today.getTime());
                if(temp != null && !temp.equals(programListTodayMinus1)) {
                    programListTodayMinus1 = temp;
                    try {
                        dataFragment.setProgramListTodayMinus1(temp);
                    } catch (Exception e) {
                        Log.e(MainActivity.TAG_REMOTELIST,"Exception while saving list to fragment");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
                            try {
                                mViewPager.getAdapter().notifyDataSetChanged();
                            } catch (IllegalStateException e) {
                                Log.w(MainActivity.TAG_REMOTELIST,"Exception while updating list: " + e.getMessage());
                            }
                        }
                    });
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //Create calendar object (today)
                Calendar today = new GregorianCalendar();
                //create parser object
                ORFParser parser = new ORFParser();

                ArrayList<ORFParser.ORFProgram> temp;

                today.add(Calendar.DAY_OF_MONTH,-2);
                temp = parser.getProgramsForDay(today.getTime());
                if(temp != null && !temp.equals(programListTodayMinus2)) {
                    programListTodayMinus2 = temp;
                    try {
                        dataFragment.setProgramListTodayMinus2(temp);
                    } catch (Exception e) {
                        Log.e(MainActivity.TAG_REMOTELIST,"Exception while saving list to fragment");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
                            try {
                                mViewPager.getAdapter().notifyDataSetChanged();
                            } catch (IllegalStateException e) {
                                Log.w(MainActivity.TAG_REMOTELIST,"Exception while updating list: " + e.getMessage());
                            }
                        }
                    });
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //Create calendar object (today)
                Calendar today = new GregorianCalendar();
                //create parser object
                ORFParser parser = new ORFParser();

                ArrayList<ORFParser.ORFProgram> temp;

                today.add(Calendar.DAY_OF_MONTH,-3);
                temp = parser.getProgramsForDay(today.getTime());
                if(temp != null && !temp.equals(programListTodayMinus3)) {
                    programListTodayMinus3 = temp;
                    try {
                        dataFragment.setProgramListTodayMinus3(temp);
                    } catch (Exception e) {
                        Log.e(MainActivity.TAG_REMOTELIST,"Exception while saving list to fragment");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
                            try {
                                mViewPager.getAdapter().notifyDataSetChanged();
                            } catch (IllegalStateException e) {
                                Log.w(MainActivity.TAG_REMOTELIST,"Exception while updating list: " + e.getMessage());
                            }
                        }
                    });
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //Create calendar object (today)
                Calendar today = new GregorianCalendar();
                //create parser object
                ORFParser parser = new ORFParser();

                ArrayList<ORFParser.ORFProgram> temp;

                today.add(Calendar.DAY_OF_MONTH,-4);
                temp = parser.getProgramsForDay(today.getTime());
                if(temp != null && !temp.equals(programListTodayMinus4)) {
                    programListTodayMinus4 = temp;
                    try {
                        dataFragment.setProgramListTodayMinus4(temp);
                    } catch (Exception e) {
                        Log.e(MainActivity.TAG_REMOTELIST,"Exception while saving list to fragment");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
                            try {
                                mViewPager.getAdapter().notifyDataSetChanged();
                            } catch (IllegalStateException e) {
                                Log.w(MainActivity.TAG_REMOTELIST,"Exception while updating list: " + e.getMessage());
                            }
                        }
                    });
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //Create calendar object (today)
                Calendar today = new GregorianCalendar();
                //create parser object
                ORFParser parser = new ORFParser();

                ArrayList<ORFParser.ORFProgram> temp;

                today.add(Calendar.DAY_OF_MONTH,-5);
                temp = parser.getProgramsForDay(today.getTime());
                if(temp != null && !temp.equals(programListTodayMinus5)) {
                    programListTodayMinus5 = temp;
                    try {
                        dataFragment.setProgramListTodayMinus5(temp);
                    } catch (Exception e) {
                        Log.e(MainActivity.TAG_REMOTELIST,"Exception while saving list to fragment");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
                            try {
                                mViewPager.getAdapter().notifyDataSetChanged();
                            } catch (IllegalStateException e) {
                                Log.w(MainActivity.TAG_REMOTELIST,"Exception while updating list: " + e.getMessage());
                            }
                        }
                    });
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //Create calendar object (today)
                Calendar today = new GregorianCalendar();
                //create parser object
                ORFParser parser = new ORFParser();

                ArrayList<ORFParser.ORFProgram> temp;

                today.add(Calendar.DAY_OF_MONTH, -6);
                temp = parser.getProgramsForDay(today.getTime());
                if (temp != null && !temp.equals(programListTodayMinus6)) {
                    programListTodayMinus6 = temp;
                    try {
                        dataFragment.setProgramListTodayMinus6(temp);
                    } catch (Exception e) {
                        Log.e(MainActivity.TAG_REMOTELIST,"Exception while saving list to fragment");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
                            try {
                                mViewPager.getAdapter().notifyDataSetChanged();
                            } catch (IllegalStateException e) {
                                Log.w(MainActivity.TAG_REMOTELIST,"Exception while updating list: " + e.getMessage());
                            }
                        }
                    });
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //Create calendar object (today)
                Calendar today = new GregorianCalendar();
                //create parser object
                ORFParser parser = new ORFParser();

                ArrayList<ORFParser.ORFProgram> temp;

                today.add(Calendar.DAY_OF_MONTH,-7);
                temp = parser.getProgramsForDay(today.getTime());
                if(temp != null && !temp.equals(programListTodayMinus7)) {
                    programListTodayMinus7 = temp;
                    try {
                        dataFragment.setProgramListTodayMinus7(temp);
                    } catch (Exception e) {
                        Log.e(MainActivity.TAG_REMOTELIST,"Exception while saving list to fragment");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
                            try {
                                mViewPager.getAdapter().notifyDataSetChanged();
                            } catch (IllegalStateException e) {
                                Log.w(MainActivity.TAG_REMOTELIST,"Exception while updating list: " + e.getMessage());
                            }
                        }
                    });
                }
            }
        }).start();
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void registerV21() {
        final IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_HDMI_AUDIO_PLUG);
        registerReceiver(mReceiverV21, intentFilter);
    }

    private final BroadcastReceiver mReceiverV21 = AndroidUtil.isLolliPopOrLater() ? new BroadcastReceiver()
    {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null)
                return;
        }
    } : null;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if( mService == null ) {
                Log.w("TAG", "Intent received, but MediaService is not loaded, skipping.");
                return;
            }

            if(action.equalsIgnoreCase(MediaService.ACTION_PLAY_PAUSE)) {
                mService.onCommand(MediaService.ACTION_PLAY_PAUSE, "");
                attachTimerMethodSeek();
            }
        }
    };

    /**
     * Regular scheduled method, updating following information:
     * Current seek time / full time of the program in the notification
     * Current seek time / full time / progress bar in the main activity
     */
    private void TimerMethodSeek() {
        if(mService != null) {
            /** manage time formats, depending on play state **/
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
                    dataFragment.setCurrentTime(timeStamp);
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
                    dataFragment.setCurrentDuration(currentDuration);
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

            /** set the corresponding notification **/
            switch(timeStamp) {
                case -1:
                    //cancel() if the playback is stopped
                    mService.stopForeground(true);
                    updateScreenNotification(NOTIFICATION_COMMAND_CANCEL, "");
                    isPausedNotified = false;
                    Log.i("NOTE","STOPPED -> cancel");
                    handler.removeCallbacks(mRunnableSeek);
                    break;
                case -2:
                    mService.stopForeground(true);
                    //if paused && not notified already (done only once)
                    if(!isPausedNotified && showPausedNotification) {
                        isPausedNotified = true;
                        updateScreenNotification(NOTIFICATION_COMMAND_UPDATE_TEXT, "Pause: " + dateString);
                        Log.i("NOTE", "PAUSED -> new notification");
                    }
                    if(!showPausedNotification) updateScreenNotification(NOTIFICATION_COMMAND_CANCEL,"");
                    handler.removeCallbacks(mRunnableSeek);
                    Log.i("NOTE","PAUSED -> already notified");
                    break;
                default:
                    //if the playback is active, display the current time
                    if(showPlayNotification) {
                        isPausedNotified = false;
                        Log.i("NOTE", "PLAY -> update...");
                        mService.startForeground(MainActivity.NOTIFICATION_PLAY_ID, updateScreenNotification(NOTIFICATION_COMMAND_UPDATE_TEXT, "Abspielen: " + dateString));
                    }
                    handler.postDelayed(mRunnableSeek, 1000);
                    break;
            }

            //Update the time in the text view (GUI, bottom right)
            TextView time = (TextView)findViewById(R.id.textViewTime);
            time.setText(dateString);
            updateGUIElementsVisibility();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Stop the regular list update
        programDataTimer.cancel();
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

    private Notification updateScreenNotification(int command, String parameter) {
        /** create the notification **/
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification mNotification;

        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(mService)
                .setContentTitle("Ö1 - PublicStream")
                .setSmallIcon(R.drawable.notification_play)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(contentIntent);

        //PLay Pause
        PendingIntent piPlayPause = PendingIntent.getBroadcast(mService, 0, new Intent(MediaService.ACTION_PLAY_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT);

        switch(command) {
            case NOTIFICATION_COMMAND_CANCEL:
                mNotificationManager.cancel(MainActivity.NOTIFICATION_PLAY_ID);
                return null;
            case NOTIFICATION_COMMAND_UPDATE_ICON:
                return null;
            case NOTIFICATION_COMMAND_UPDATE_TEXT:
                mNotifyBuilder.setContentText(parameter);
                if(mService.getState().equals(MediaService.MEDIA_STATE_PLAYING)) {
                    mNotifyBuilder.addAction(R.drawable.ic_av_pause_circle_outline,"Pause",piPlayPause);
                } else {
                    mNotifyBuilder.addAction(R.drawable.ic_av_play_circle_outline,"Play",piPlayPause);
                }
                mNotification = mNotifyBuilder.build();
                mNotificationManager.notify(
                        MainActivity.NOTIFICATION_PLAY_ID, mNotification);

                return mNotification;
            case NOTIFICATION_COMMAND_NOTHING:
                return mNotifyBuilder.build();
            default:
                return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_DATE = "sectionDate";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_DATE, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            ListView expandableList = (ListView) rootView.findViewById(R.id.programList);
            ListView headerList = (ListView) rootView.findViewById(R.id.headerList);
            headerAdapter = new HeaderAdapter(getArguments().getInt(ARG_SECTION_DATE));
            headerAdapter.setInflater(getActivity().getLayoutInflater());

            switch(getArguments().getInt(ARG_SECTION_DATE)) {
                case 1: //Offline
                    adapter = new ProgramExpandableAdapter(true);
                    adapter.setInflater(getActivity().getLayoutInflater(), getContext());
                    adapter.setListPrograms(programListOffline);
                    break;
                case 2: //Today
                    adapter = new ProgramExpandableAdapter(false);
                    adapter.setInflater(getActivity().getLayoutInflater(), getContext());
                    adapter.setListPrograms(programListToday);
                    break;
                case 3: //Today -1 day
                    adapter = new ProgramExpandableAdapter(false);
                    adapter.setInflater(getActivity().getLayoutInflater(), getContext());
                    adapter.setListPrograms(programListTodayMinus1);
                    break;
                case 4: //Today -2 day
                    adapter = new ProgramExpandableAdapter(false);
                    adapter.setInflater(getActivity().getLayoutInflater(), getContext());
                    adapter.setListPrograms(programListTodayMinus2);
                    break;
                case 5: //Today -3 day
                    adapter = new ProgramExpandableAdapter(false);
                    adapter.setInflater(getActivity().getLayoutInflater(), getContext());
                    adapter.setListPrograms(programListTodayMinus3);
                    break;
                case 6: //Today -4 day
                    adapter = new ProgramExpandableAdapter(false);
                    adapter.setInflater(getActivity().getLayoutInflater(), getContext());
                    adapter.setListPrograms(programListTodayMinus4);
                    break;
                case 7: //Today -5 day
                    adapter = new ProgramExpandableAdapter(false);
                    adapter.setInflater(getActivity().getLayoutInflater(), getContext());
                    adapter.setListPrograms(programListTodayMinus5);
                    break;
                case 8: //Today -6 day
                    adapter = new ProgramExpandableAdapter(false);
                    adapter.setInflater(getActivity().getLayoutInflater(), getContext());
                    adapter.setListPrograms(programListTodayMinus6);
                    break;
                case 9: //Today -7 day
                    adapter = new ProgramExpandableAdapter(false);
                    adapter.setInflater(getActivity().getLayoutInflater(), getContext());
                    adapter.setListPrograms(programListTodayMinus7);
                    break;
            }
            expandableList.setAdapter(adapter);
            headerList.setAdapter(headerAdapter);
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            // Show 9 total pages (7days in the past, today and the offline parts)
            return 9;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }
}

