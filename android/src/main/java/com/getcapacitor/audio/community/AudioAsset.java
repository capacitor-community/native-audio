package com.getcapacitor.audio.community;

import android.content.res.AssetFileDescriptor;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class AudioAsset {

    private final String TAG = "AudioAsset";

    private ArrayList<AudioDispatcher> audioList;
    private int playIndex = 0;

    AudioAsset(AssetFileDescriptor assetFileDescriptor, int audioChannelNum, float volume) throws Exception {
        audioList = new ArrayList<>();

        if (audioChannelNum < 0) {
            audioChannelNum = 1;
        }

        for (int x = 0; x < audioChannelNum; x++) {
            AudioDispatcher audioDispatcher = new AudioDispatcher(assetFileDescriptor, volume);
            audioList.add(audioDispatcher);
        }
    }

    public void play(Callable<Void> callback) throws Exception {
        AudioDispatcher audio = audioList.get(playIndex);

        if (audio != null) {
            audio.play(callback);
            playIndex++;
            playIndex = playIndex % audioList.size();
        }
    }

    public boolean pause() throws Exception {
        boolean wasPlaying = false;

        for (int x = 0; x < audioList.size(); x++) {
            AudioDispatcher audio = audioList.get(x);
            wasPlaying |= audio.pause();
        }

        return wasPlaying;
    }

    public void resume() throws Exception {
        if (audioList.size() > 0) {
            AudioDispatcher audio = audioList.get(0);

            if (audio != null) {
                audio.resume();
            }
        }
    }

    public void stop() throws Exception {
        for (int x = 0; x < audioList.size(); x++) {
            AudioDispatcher audio = audioList.get(x);

            if (audio != null) {
                audio.stop();
            }
        }
    }

    public void loop() throws Exception {
        AudioDispatcher audio = audioList.get(playIndex);

        if (audio != null) {
            audio.loop();
            playIndex++;
            playIndex = playIndex % audioList.size();
        }
    }

    public void unload() throws Exception {
        this.stop();

        for (int x = 0; x < audioList.size(); x++) {
            AudioDispatcher audio = audioList.get(x);

            if (audio != null) {
                audio.unload();
            }
        }

        audioList.clear();
    }

    public void setVolume(float volume) throws Exception {
        for (int x = 0; x < audioList.size(); x++) {
            AudioDispatcher audio = audioList.get(x);

            if (audio != null) {
                audio.setVolume(volume);
            }
        }
    }

}
