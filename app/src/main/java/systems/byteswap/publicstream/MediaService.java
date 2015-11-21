package systems.byteswap.publicstream;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;

//TODO: am ende des streams -> service beenden...
//TODO: duration anders erledigen...

public class MediaService extends Service implements MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener {
    public final static String ACTION_PLAY_PAUSE = "systems.byteswap.action.PLAY";
    public final static String ACTION_STOP = "systems.byteswap.action.STOP";
    public final static String ACTION_SETTIME = "systems.byteswap.action.SETTIME";
    public final static String ACTION_LOAD = "systems.byteswap.action.LOAD";

    public final static String MEDIA_STATE_IDLE = "systems.byteswap.mediastate.IDLE";
    public final static String MEDIA_STATE_PLAYING = "systems.byteswap.mediastate.PLAYING";
    public final static String MEDIA_STATE_PAUSED = "systems.byteswap.mediastate.PAUSED";
    public final static String MEDIA_STATE_PREPARING = "systems.byteswap.mediastate.PREPARING";

    private MediaPlayer mMediaPlayer = null;
    private String mState = MEDIA_STATE_IDLE;

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder<MediaService>(this);
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public boolean onCommand(String command, String parameter) {
        switch(command) {
            case ACTION_PLAY_PAUSE:
                switch(mState) {
                    case MEDIA_STATE_PAUSED:
                        mMediaPlayer.start();
                        mState = MEDIA_STATE_PLAYING;
                        break;
                    case MEDIA_STATE_PLAYING:
                        mMediaPlayer.pause();
                        mState = MEDIA_STATE_PAUSED;
                        break;
                }
                break;
            case ACTION_LOAD:
                try {
                    switch(mState) {
                        //reset the mediaplayer if anything is running...
                        case MEDIA_STATE_PLAYING:
                        case MEDIA_STATE_PAUSED:
                        case MEDIA_STATE_PREPARING:
                            mMediaPlayer.reset();
                            mState = MEDIA_STATE_IDLE;
                            break;
                        default:
                            break;
                    }
                    mMediaPlayer.setDataSource(parameter);
                    mMediaPlayer.prepareAsync();
                    mState = MEDIA_STATE_PREPARING;
                } catch (IOException e) {
                    mMediaPlayer.reset();
                    mState = MEDIA_STATE_IDLE;
                    Toast.makeText(MediaService.this, "Ups, URL nicht gültig...", Toast.LENGTH_SHORT).show();
                }
                break;
            case ACTION_SETTIME:
                break;
            default:
                return false;

        }
        return true;
    }

    @Override
    public void onDestroy() {
        if(mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        mState = MEDIA_STATE_PLAYING;
        player.start();
    }

    public int getDuration() {
        switch(mState) {
            case MEDIA_STATE_PLAYING:
                return mMediaPlayer.getDuration();
            case MEDIA_STATE_PAUSED:
                return -2;
            default:
                return -1;
        }
    }

    public int getCurrentPosition() {
        switch(mState) {
            case MEDIA_STATE_PLAYING:
                return mMediaPlayer.getCurrentPosition();
            case MEDIA_STATE_PAUSED:
                return -2;
            default:
                return -1;
        }
    }
}
