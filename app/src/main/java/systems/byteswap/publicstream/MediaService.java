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
 **/

package systems.byteswap.publicstream;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

//TODO: irgendwie wird hier mit der Mediaplayerinstanz geschlampt...
//TODO: eventuell auch im Fragment speichern?
//TODO: doku fertig machen
//TODO: eventuell wifilock nicht nehmen, wenn man offline abspielt?
//TODO: Log.e/d mit if(D) machen...


/**
 * The media service class provides an interface to the VLC player as a service.
 *
 * It can be controlled via onCommand().
 * The telephone state is also handled here
 *
 * Via start/stopForeground the service can be brought to foreground, enabling a jitter free playback
 *
 * In some occasions there might be a problem with the wifi/wakelocks. Should be investigated sometime.
 */
public class MediaService extends Service implements IVLCVout.Callback, LibVLC.HardwareAccelerationError, Media.EventListener {
    /** Toggle play and pause of the media player, parameters: none */
    public final static String ACTION_PLAY_PAUSE = "systems.byteswap.action.PLAYPAUSE";
    /** Play (no toggle) of the media player, parameters: none */
    public final static String ACTION_PLAY = "systems.byteswap.action.PLAY";
    /** Pause (no toggle) of the media player, parameters: none */
    public final static String ACTION_PAUSE = "systems.byteswap.action.PAUSE";
    /** Stop (no toggle and no play again, restart with "load") of the media player, parameters: none */
    public final static String ACTION_STOP = "systems.byteswap.action.STOP";
    /** Set the progress of the media player, parameters: progress from 0.0 to 1.0 (float -> string) */
    public final static String ACTION_SETTIME = "systems.byteswap.action.SETTIME";
    /** Load an URL or a path (you need to "play" afterwards), parameters: path or URL (string) */
    public final static String ACTION_LOAD = "systems.byteswap.action.LOAD";

    /** state of the mediaplayer: idle, no media loaded. Possible commands:
     * ACTION_LOAD
     * */
    public final static String MEDIA_STATE_IDLE = "systems.byteswap.mediastate.IDLE";
    /** state of the mediaplayer: playing, media loaded. Possible commands:
     * ACTION_LOAD
     * ACTION_PLAY_PAUSE
     * ACTION_PAUSE
     * ACTION_STOP
     * ACTION_SETTIME
     * */
    public final static String MEDIA_STATE_PLAYING = "systems.byteswap.mediastate.PLAYING";
    /** state of the mediaplayer: paused, media loaded. Possible commands:
     * ACTION_LOAD
     * ACTION_PLAY_PAUSE
     * ACTION_PLAY
     * ACTION_STOP
     * ACTION_SETTIME
     * */
    public final static String MEDIA_STATE_PAUSED = "systems.byteswap.mediastate.PAUSED";

    public final static String TAG = "MediaService";
    public final static boolean D = false;
    public final static boolean E = true;

    private MediaPlayer mMediaPlayer = null;
    private LibVLC libvlc;
    private String mState = MEDIA_STATE_IDLE;
    private final IBinder mBinder = new LocalBinder();
    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);
    private WifiManager.WifiLock wifiLock;
    private PowerManager.WakeLock wakeLock;
    private boolean isLive = false;
    private boolean useHWAcceleration = false;
    PhoneStateListener phoneStateListener;

    public MediaService() {
    }

    @Override
    public void onCreate() {
        //create a wifilock to keep the wificonnection up if playing
        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "publicStreamWifiLock");
        //create a wakelock to keep the CPU running, if playing
        wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "publicStreamWakeLock");
    }

    @Override
    public void onEvent(Media.Event event) {
        if(D) Log.d(TAG, event.toString());
    }

    /**
     * Switch on/off the hardware acceleration of the LibVLC.
     * Sometimes, the HW acceleration is not playing well, so it can be disabled, e.g. via
     * the settings
     *
     * @param useHWAcceleration true, if you want to use the HW acceleration; false if not. Default: false
     */
    public void setUseHWAcceleration(boolean useHWAcceleration) {
        this.useHWAcceleration = useHWAcceleration;
    }


    private class LocalBinder extends Binder {
        MediaService getService() {
            return MediaService.this;
        }
    }
    public static MediaService getService(IBinder iBinder) {
        LocalBinder binder = (LocalBinder) iBinder;
        return binder.getService();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mState.equals(MEDIA_STATE_IDLE)) stopSelf();
        return true;
    }

    /**
     * This is the primary control method for the media player, it is used to control a
     * running media player. All available commands and their description is available in the
     * beginning of this class.
     *
     * @param command current command to control the media player, see MediaService.ACTION_*
     * @param parameter Unified parameter, by now it is always the URL/path to load
     * @return true, if everything went fine; false otherwise (no error codes provided)
     */
    public boolean onCommand(String command, String parameter) {
        TelephonyManager mgr;
        switch(command) {
            case ACTION_PLAY_PAUSE:
                switch(mState) {
                    case MEDIA_STATE_PAUSED:
                        wifiLock.acquire();
                        wakeLock.acquire();
                        mMediaPlayer.play();
                        mState = MEDIA_STATE_PLAYING;

                        mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                        if(mgr != null) {
                            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                        }
                        break;
                    case MEDIA_STATE_PLAYING:
                        mMediaPlayer.pause();
                        mState = MEDIA_STATE_PAUSED;
                        wifiLock.release();
                        wakeLock.release();

                        mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                        if(mgr != null) {
                            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
                        }
                        break;
                }
                break;
            case ACTION_PAUSE:
                switch(mState) {
                    case MEDIA_STATE_PLAYING:
                        mMediaPlayer.pause();
                        mState = MEDIA_STATE_PAUSED;
                        wifiLock.release();
                        wakeLock.release();

                        mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                        if(mgr != null) {
                            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
                        }
                        break;
                }
                break;
            case ACTION_PLAY:
                switch(mState) {
                    case MEDIA_STATE_PAUSED:
                        wifiLock.acquire();
                        wakeLock.acquire();
                        mMediaPlayer.play();
                        mState = MEDIA_STATE_PLAYING;

                        mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                        if(mgr != null) {
                            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                        }
                        break;
                }
                break;
            case ACTION_STOP:
                switch(mState) {
                    case MEDIA_STATE_PAUSED:
                    case MEDIA_STATE_PLAYING:
                        mMediaPlayer.stop();
                        mState = MEDIA_STATE_IDLE;
                        wifiLock.release();
                        wakeLock.release();

                        mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                        if(mgr != null) {
                            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
                        }
                        break;
                }
                break;
            case ACTION_LOAD:
                    isLive = parameter.equals(ORFParser.ORF_LIVE_URL);
                    createPlayer(parameter);
                    wifiLock.acquire();
                    wakeLock.acquire();
                    mState = MEDIA_STATE_PLAYING;

                    mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    if(mgr != null) {
                        mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                    }
                break;
            case ACTION_SETTIME:
                //float position = Float.valueOf(parameter);
                if(mMediaPlayer != null) {
                    mMediaPlayer.setPosition(Float.valueOf(parameter));
                }
                break;
            default:
                return false;

        }
        return true;
    }

    @Override
    public void onDestroy() {
        if(mMediaPlayer != null && this.getState().equals(MediaService.MEDIA_STATE_IDLE)) {
            try {
                mMediaPlayer.release();
            } catch (Exception e) {
                Log.e("MEDIASERVICE_DESTROY",e.getMessage());
            }
            try {
                wifiLock.release();
            } catch (Exception e) {
                Log.e("MEDIASERVICE_DESTROY",e.getMessage());
            }
            try {
                wakeLock.release();
            } catch (Exception e) {
                Log.e("MEDIASERVICE_DESTROY",e.getMessage());
            }
            mMediaPlayer = null;
        }
    }

    public String getState() {
        return this.mState;
    }

    /**
     * TODO: doku schreiben
     * @return
     */
    public int getDuration() {
        switch(mState) {
            case MEDIA_STATE_PLAYING:
                try {
                    return safeLongToInt(mMediaPlayer.getLength());
                } catch (NullPointerException e) {
                    return -1;
                }
            case MEDIA_STATE_PAUSED:
                return -2;
            default:
                return -1;
        }
    }

    public int getCurrentPosition() {
        switch(mState) {
            case MEDIA_STATE_PLAYING:
                try {
                    return safeLongToInt(mMediaPlayer.getTime());
                } catch (NullPointerException e) {
                    return 0;
                }
            case MEDIA_STATE_PAUSED:
                return -2;
            default:
                return -1;
        }
    }

    @Override
    public void onNewLayout(IVLCVout ivlcVout, int i, int i1, int i2, int i3, int i4, int i5) {

    }

    @Override
    public void onSurfacesCreated(IVLCVout ivlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout ivlcVout) {

    }

    @Override
    public void eventHardwareAccelerationError() {
        Toast.makeText(MediaService.this, "HardwareAccelerationError...", Toast.LENGTH_SHORT).show();
    }

    /**
     * TODO: doku schreiben
     * @param media
     */
    private void createPlayer(String media) {
        releasePlayer();
        try {
            // Create LibVLC
            ArrayList<String> options = new ArrayList<>();
            options.add("--aout=opensles");
            //options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            options.add("--network-caching=" + 6000);

            libvlc = new LibVLC(options);
            libvlc.setOnHardwareAccelerationError(this);

            // Create media player
            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);

            Media m;
            if(media.contains("http")) {
                m = new Media(libvlc, Uri.parse(media));
            } else {
                m = new Media(libvlc,media);
            }
            m.setEventListener(this);
            m.addOption(":network-caching=6000");
            //disable hardware decoder if defined in the settings, this may use more battery, but it works
            //without audio stuttering on some devices

            m.setHWDecoderEnabled(false,false);
            mMediaPlayer.setMedia(m);
            m.release();
            mMediaPlayer.play();
        } catch (Exception e) {
            Toast.makeText(MediaService.this, "Fehler beim Erstellen des Players...", Toast.LENGTH_SHORT).show();
            Log.e("PUBLICSTREAM", "Error creating player: " + e.getMessage());
        }
    }

    private void releasePlayer() {
        if (libvlc == null)
            return;
        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        if(libvlc != null) {
            libvlc.release();
            libvlc = null;
        }
    }

    public static int safeLongToInt(long l) {
        int i = (int)l;
        if ((long)i != l) {
            throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
        }
        return i;
    }

    public void setState(String state) {
        this.mState = state;
    }

    public boolean isLive() {
        return isLive;
    }

    private static class MyPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<MediaService> mOwner;

        public MyPlayerListener(MediaService owner) {
            //mOwner = new WeakReference<MediaService>(owner);
            mOwner = new WeakReference<>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            MediaService player = mOwner.get();

            switch(event.type) {
                case MediaPlayer.Event.EndReached:
                    player.setState(MediaService.MEDIA_STATE_IDLE);
                    player.releasePlayer();
                    player = null;
                    break;
                case MediaPlayer.Event.Playing:
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                default:
                    break;
            }
        }
    }
}
