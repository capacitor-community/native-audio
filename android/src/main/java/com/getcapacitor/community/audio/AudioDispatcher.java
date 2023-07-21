package com.getcapacitor.community.audio;

import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import java.util.concurrent.Callable;

public class AudioDispatcher
    implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {

    private final String TAG = "AudioDispatcher";

    private final int INVALID = 0;
    private final int PREPARED = 1;
    private final int PENDING_PLAY = 2;
    private final int PLAYING = 3;
    private final int PENDING_LOOP = 4;
    private final int LOOPING = 5;
    private final int PAUSE = 6;

    private MediaPlayer mediaPlayer;
    private int mediaState;
    private AudioAsset owner;

    public AudioDispatcher(AssetFileDescriptor assetFileDescriptor, float volume) throws Exception {
        mediaState = INVALID;

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setDataSource(
            assetFileDescriptor.getFileDescriptor(),
            assetFileDescriptor.getStartOffset(),
            assetFileDescriptor.getLength()
        );
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setAudioAttributes(
            new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        );
        mediaPlayer.setVolume(volume, volume);
        mediaPlayer.prepare();
    }

    public void setOwner(AudioAsset asset) {
        owner = asset;
    }

    public double getDuration() {
        return mediaPlayer.getDuration() / 1000.0;
    }

    public double getCurrentPosition() {
        return mediaPlayer.getCurrentPosition() / 1000.0;
    }

    public void play(Double time, Callable<Void> callable) throws Exception {
        invokePlay(time, false);
        callable.call();
    }

    public boolean pause() throws Exception {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaState = PAUSE;
            return true;
        }

        return false;
    }

    public void resume() throws Exception {
        mediaPlayer.start();
    }

    public void stop() throws Exception {
        if (mediaPlayer.isPlaying()) {
            mediaState = INVALID;
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
        }
    }

    public void setVolume(float volume) throws Exception {
        mediaPlayer.setVolume(volume, volume);
    }

    public void loop() throws Exception {
        mediaPlayer.setLooping(true);
    }

    public void unload() throws Exception {
        this.stop();
        mediaPlayer.release();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        try {
            if (mediaState != LOOPING) {
                this.mediaState = INVALID;

                this.stop();

                if (this.owner != null) {
                    this.owner.dispatchComplete();
                }
            }
        } catch (Exception ex) {
            Log.d(TAG, "Caught exception while listening for onCompletion: " + ex.getLocalizedMessage());
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        try {
            if (mediaState == PENDING_PLAY) {
                mediaPlayer.setLooping(false);
            } else if (mediaState == PENDING_LOOP) {
                mediaPlayer.setLooping(true);
            } else {
                mediaState = PREPARED;
            }
        } catch (Exception ex) {
            Log.d(TAG, "Caught exception while listening for onPrepared: " + ex.getLocalizedMessage());
        }
    }

    private void seek(Double time) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaPlayer.seekTo((int) (time * 1000), MediaPlayer.SEEK_NEXT_SYNC);
        } else {
            mediaPlayer.seekTo((int) (time * 1000));
        }
    }

    private void invokePlay(Double time, Boolean loop) {
        try {
            boolean playing = mediaPlayer.isPlaying();

            if (playing) {
                mediaPlayer.pause();
                mediaPlayer.setLooping(loop);
                mediaState = PENDING_PLAY;
                seek(time);
            } else {
                if (mediaState == PREPARED) {
                    mediaState = (loop ? PENDING_LOOP : PENDING_PLAY);
                    onPrepared(mediaPlayer);
                    seek(time);
                } else {
                    mediaState = (loop ? PENDING_LOOP : PENDING_PLAY);
                    mediaPlayer.setLooping(loop);
                    seek(time);
                }
            }
        } catch (Exception ex) {
            Log.d(TAG, "Caught exception while invoking audio: " + ex.getLocalizedMessage());
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (mediaState == PENDING_PLAY || mediaState == PENDING_LOOP) {
            Log.w("AudioDispatcher", "play " + mediaState);
            mediaPlayer.start();
            mediaState = PLAYING;
        }
    }

    public boolean isPlaying() throws Exception {
        return mediaPlayer.isPlaying();
    }
}
