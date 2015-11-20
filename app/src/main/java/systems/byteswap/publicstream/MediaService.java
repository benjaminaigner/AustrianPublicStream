package systems.byteswap.publicstream;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

//TODO: alles testen & machen
//TODO: seekbar updaten...

public class MediaService extends Service implements MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener {
    private static final String ACTION_PLAY = "systems.byteswap.action.PLAY";
    private static final String ACTION_PAUSE = "systems.byteswap.action.PAUSE";
    private static final String ACTION_STOP = "systems.byteswap.action.STOP";
    private static final String ACTION_SETTIME = "systems.byteswap.action.SETTIME";

    MediaPlayer mMediaPlayer = null;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Binder to access the media service
     */
    public class MediaBinder extends Binder {
        MediaService getService() {
            return MediaService.this;
        }
    }



    public int onCommand(Intent intent, String action) {
        switch(action) {
            //TODO: mediaplayer steuern
            case ACTION_PLAY:
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.prepareAsync(); // prepare async to not block main thread
                break;
            case ACTION_PAUSE:
                break;
            case ACTION_SETTIME:
                break;
            case ACTION_STOP:
                break;
            default:
                break;
        }
        return 0;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
            player.start();
        }
}
