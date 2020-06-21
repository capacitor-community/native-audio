package com.getcapacitor.audio.community;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static com.getcapacitor.audio.community.Constant.ASSET_ID;
import static com.getcapacitor.audio.community.Constant.ASSET_PATH;
import static com.getcapacitor.audio.community.Constant.AUDIO_CHANNEL_NUM;
import static com.getcapacitor.audio.community.Constant.ERROR_ASSET_NOT_LOADED;
import static com.getcapacitor.audio.community.Constant.ERROR_ASSET_PATH_MISSING;
import static com.getcapacitor.audio.community.Constant.ERROR_AUDIO_ASSET_MISSING;
import static com.getcapacitor.audio.community.Constant.ERROR_AUDIO_EXISTS;
import static com.getcapacitor.audio.community.Constant.ERROR_AUDIO_ID_MISSING;
import static com.getcapacitor.audio.community.Constant.LOOP;
import static com.getcapacitor.audio.community.Constant.OPT_FADE_MUSIC;
import static com.getcapacitor.audio.community.Constant.VOLUME;

@NativePlugin()
public class NativeAudio extends Plugin implements AudioManager.OnAudioFocusChangeListener {

    public static final String TAG = "AudioFile";

    private static HashMap<String, AudioAsset> audioAssetList;
    private static ArrayList<AudioAsset> resumeList;
    private boolean fadeMusic = false;

    @Override
    public void load() {
        super.load();

        AudioManager audioManager = (AudioManager) getBridge().getActivity().getSystemService(Context.AUDIO_SERVICE);

        if (audioManager != null) {
            int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {

        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {

        }
    }

    @Override
    protected void handleOnPause() {
        super.handleOnPause();

        try {
            if (audioAssetList != null) {
                for (HashMap.Entry<String, AudioAsset> entry : audioAssetList.entrySet()) {
                    AudioAsset audio = entry.getValue();

                    if (audio != null) {
                        boolean wasPlaying = audio.pause();

                        if (wasPlaying) {
                            resumeList.add(audio);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.d(TAG, "Exception caught while listening for handleOnPause: " + ex.getLocalizedMessage());
        }
    }

    @Override
    protected void handleOnResume() {
        super.handleOnResume();

        try {
            if (resumeList != null) {
                while (!resumeList.isEmpty()) {
                    AudioAsset audio = resumeList.remove(0);

                    if (audio != null) {
                        audio.resume();
                    }
                }
            }
        } catch (Exception ex) {
            Log.d(TAG, "Exception caught while listening for handleOnResume: " + ex.getLocalizedMessage());
        }
    }

    @PluginMethod()
    public void configure(PluginCall call) {
        initSoundPool();

        if (call.hasOption(OPT_FADE_MUSIC)) this.fadeMusic = call.getBoolean(OPT_FADE_MUSIC);
    }

    @PluginMethod()
    public void preloadSimple(final PluginCall call) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                preloadAsset(call);
            }
        }).start();
    }

    @PluginMethod()
    public void preloadComplex(final PluginCall call) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                preloadAsset(call);
            }
        }).start();
    }

    @PluginMethod()
    public void play(final PluginCall call) {
        getBridge().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playOrLoop("play", call);
            }
        });
    }

    @PluginMethod()
    public void loop(final PluginCall call) {
        getBridge().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playOrLoop("loop", call);
            }
        });
    }

    @PluginMethod()
    public void pause(PluginCall call) {
        try {
            initSoundPool();
            String audioId = call.getString(ASSET_ID);

            if (audioAssetList.containsKey(audioId)) {
                AudioAsset asset = audioAssetList.get(audioId);
                if (asset != null) {
                    boolean wasPlaying = asset.pause();

                    if (wasPlaying) {
                        resumeList.add(asset);
                    }
                }
            } else {
                call.error(ERROR_ASSET_NOT_LOADED + " - " + audioId);
            }
        } catch (Exception ex) {
            call.error(ex.getMessage());
        }
    }

    @PluginMethod()
    public void resume(PluginCall call) {
        try {
            initSoundPool();
            String audioId = call.getString(ASSET_ID);

            if (audioAssetList.containsKey(audioId)) {
                AudioAsset asset = audioAssetList.get(audioId);
                if (asset != null) {
                    asset.resume();
                    resumeList.add(asset);
                }
            } else {
                call.error(ERROR_ASSET_NOT_LOADED + " - " + audioId);
            }
        } catch (Exception ex) {
            call.error(ex.getMessage());
        }
    }

    @PluginMethod()
    public void stop(PluginCall call) {
        try {
            initSoundPool();
            String audioId = call.getString(ASSET_ID);

            if (audioAssetList.containsKey(audioId)) {
                AudioAsset asset = audioAssetList.get(audioId);
                if (asset != null) {
                    asset.stop();
                }
            } else {
                call.error(ERROR_ASSET_NOT_LOADED + " - " + audioId);
            }
        } catch (Exception ex) {
            call.error(ex.getMessage());
        }
    }

    @PluginMethod()
    public void unload(PluginCall call) {
        try {
            initSoundPool();
            new JSObject();
            JSObject status;

            if (isStringValid(call.getString(ASSET_ID))) {
                String audioId = call.getString(ASSET_ID);

                if (audioAssetList.containsKey(audioId)) {
                    AudioAsset asset = audioAssetList.get(audioId);
                    if (asset != null) {
                        asset.unload();
                        audioAssetList.remove(audioId);

                        status = new JSObject();
                        status.put("status", "OK");
                        call.success(status);
                    } else {
                        status = new JSObject();
                        status.put("status", false);
                        call.success(status);
                    }
                } else {
                    status = new JSObject();
                    status.put("status", ERROR_AUDIO_ASSET_MISSING + " - " + audioId);
                    call.success(status);
                }
            } else {
                status = new JSObject();
                status.put("status", ERROR_AUDIO_ID_MISSING);
                call.success(status);
            }
        } catch (Exception ex) {
            call.error(ex.getMessage());
        }
    }

    @PluginMethod()
    public void setVolume(PluginCall call) {
        try {
            initSoundPool();

            String audioId = call.getString(ASSET_ID);
            float volume = call.getFloat(VOLUME);

            if (audioAssetList.containsKey(audioId)) {
                AudioAsset asset = audioAssetList.get(audioId);
                if (asset != null) {
                    asset.setVolume(volume);
                }
            } else {
                call.error(ERROR_AUDIO_ASSET_MISSING);
            }
        } catch (Exception ex) {
            call.error(ex.getMessage());
        }
    }

    private void preloadAsset(PluginCall call) {
        double volume = 1.0;
        int audioChannelNum = 1;

        try {
            initSoundPool();

            String audioId = call.getString(ASSET_ID);

            if (!isStringValid(audioId)) {
                call.error(ERROR_AUDIO_ID_MISSING + " - " + audioId);
                return;
            }

            if (!audioAssetList.containsKey(audioId)) {
                String assetPath = call.getString(ASSET_PATH);

                if (!isStringValid(assetPath)) {
                    call.error(ERROR_ASSET_PATH_MISSING + " - " + audioId + " - " + assetPath);
                    return;
                }

                String fullPath = "raw/".concat(assetPath);

                if (call.getDouble(VOLUME) == null) {
                    volume = 1.0;
                } else {
                    volume = call.getDouble(VOLUME, 0.5);
                }

                if (call.getInt(AUDIO_CHANNEL_NUM) == null) {
                    audioChannelNum = 1;
                } else {
                    audioChannelNum = call.getInt(AUDIO_CHANNEL_NUM);
                }

                Context ctx = getBridge().getActivity().getApplicationContext();
                AssetManager am = ctx.getResources().getAssets();
                AssetFileDescriptor assetFileDescriptor = am.openFd(fullPath);

                AudioAsset asset = new AudioAsset(assetFileDescriptor, audioChannelNum, (float) volume);
                audioAssetList.put(audioId, asset);

                JSObject status = new JSObject();
                status.put("STATUS", "OK");
                call.success(status);
            } else {
                call.error(ERROR_AUDIO_EXISTS);
            }
        } catch (Exception ex) {
            call.error(ex.getMessage());
        }
    }

    private void playOrLoop(String action, final PluginCall call) {
        try {
            initSoundPool();

            final String audioId = call.getString(ASSET_ID);

            if (audioAssetList.containsKey(audioId)) {
                AudioAsset asset = audioAssetList.get(audioId);
                if (LOOP.equals(action) && asset != null) {
                    asset.loop();
                } else if (asset != null) {
                    asset.play(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            call.success(new JSObject().put(ASSET_ID, audioId));

                            return null;
                        }
                    });
                }
            }
        } catch (Exception ex) {
            call.error(ex.getMessage());
        }
    }

    private void initSoundPool() {
        if (audioAssetList == null) {
            audioAssetList = new HashMap<>();
        }

        if (resumeList == null) {
            resumeList = new ArrayList<>();
        }
    }

    private boolean isStringValid(String value) {
        return (value != null && !value.isEmpty() && !value.equals("null"));
    }

}
