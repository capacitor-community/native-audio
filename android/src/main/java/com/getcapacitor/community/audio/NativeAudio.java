package com.getcapacitor.community.audio;

import static com.getcapacitor.community.audio.Constant.ASSET_ID;
import static com.getcapacitor.community.audio.Constant.ASSET_PATH;
import static com.getcapacitor.community.audio.Constant.AUDIO_CHANNEL_NUM;
import static com.getcapacitor.community.audio.Constant.ERROR_ASSET_NOT_LOADED;
import static com.getcapacitor.community.audio.Constant.ERROR_ASSET_PATH_MISSING;
import static com.getcapacitor.community.audio.Constant.ERROR_AUDIO_ASSET_MISSING;
import static com.getcapacitor.community.audio.Constant.ERROR_AUDIO_EXISTS;
import static com.getcapacitor.community.audio.Constant.ERROR_AUDIO_ID_MISSING;
import static com.getcapacitor.community.audio.Constant.LOOP;
import static com.getcapacitor.community.audio.Constant.OPT_FADE_MUSIC;
import static com.getcapacitor.community.audio.Constant.OPT_FOCUS_AUDIO;
import static com.getcapacitor.community.audio.Constant.VOLUME;

import android.Manifest;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

@CapacitorPlugin(
    permissions = {
        @Permission(strings = { Manifest.permission.MODIFY_AUDIO_SETTINGS }),
        @Permission(strings = { Manifest.permission.WRITE_EXTERNAL_STORAGE }),
        @Permission(strings = { Manifest.permission.READ_PHONE_STATE })
    }
)
public class NativeAudio extends Plugin implements AudioManager.OnAudioFocusChangeListener {

    public static final String TAG = "NativeAudio";

    private static HashMap<String, AudioAsset> audioAssetList;
    private static ArrayList<AudioAsset> resumeList;
    private boolean fadeMusic = false;
    private AudioManager audioManager;

    @Override
    public void load() {
        super.load();

        this.audioManager = (AudioManager) getBridge().getActivity().getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {} else if (
            focusChange == AudioManager.AUDIOFOCUS_LOSS
        ) {}
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

    @PluginMethod
    public void configure(PluginCall call) {
        initSoundPool();

        this.fadeMusic = call.getBoolean(OPT_FADE_MUSIC, false);

        if (this.audioManager != null) {
            if (call.getBoolean(OPT_FOCUS_AUDIO, false)) {
                this.audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            } else {
                this.audioManager.abandonAudioFocus(this);
            }
        }
        call.resolve();
    }

    @PluginMethod
    public void preload(final PluginCall call) {
        new Thread(() -> preloadAsset(call)).start();
    }

    @PluginMethod
    public void play(final PluginCall call) {
        getBridge()
            .getActivity()
            .runOnUiThread(() -> playOrLoop("play", call));
    }

    @PluginMethod
    public void getCurrentTime(final PluginCall call) {
        try {
            initSoundPool();

            String audioId = call.getString(ASSET_ID);

            if (!isStringValid(audioId)) {
                call.reject(ERROR_AUDIO_ID_MISSING + " - " + audioId);
                return;
            }

            if (audioAssetList.containsKey(audioId)) {
                AudioAsset asset = audioAssetList.get(audioId);
                if (asset != null) {
                    call.resolve(new JSObject().put("currentTime", asset.getCurrentPosition()));
                }
            } else {
                call.reject(ERROR_AUDIO_ASSET_MISSING + " - " + audioId);
            }
        } catch (Exception ex) {
            call.reject(ex.getMessage());
        }
    }

    @PluginMethod
    public void getDuration(final PluginCall call) {
        try {
            initSoundPool();

            String audioId = call.getString(ASSET_ID);

            if (!isStringValid(audioId)) {
                call.reject(ERROR_AUDIO_ID_MISSING + " - " + audioId);
                return;
            }

            if (audioAssetList.containsKey(audioId)) {
                AudioAsset asset = audioAssetList.get(audioId);
                if (asset != null) {
                    call.resolve(new JSObject().put("duration", asset.getDuration()));
                }
            } else {
                call.reject(ERROR_AUDIO_ASSET_MISSING + " - " + audioId);
            }
        } catch (Exception ex) {
            call.reject(ex.getMessage());
        }
    }

    @PluginMethod
    public void loop(final PluginCall call) {
        getBridge()
            .getActivity()
            .runOnUiThread(() -> playOrLoop("loop", call));
    }

    @PluginMethod
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

                    call.resolve();
                }
            } else {
                call.reject(ERROR_ASSET_NOT_LOADED + " - " + audioId);
            }
        } catch (Exception ex) {
            call.reject(ex.getMessage());
        }
    }

    @PluginMethod
    public void resume(PluginCall call) {
        try {
            initSoundPool();
            String audioId = call.getString(ASSET_ID);

            if (audioAssetList.containsKey(audioId)) {
                AudioAsset asset = audioAssetList.get(audioId);
                if (asset != null) {
                    asset.resume();
                    resumeList.add(asset);
                    call.resolve();
                }
            } else {
                call.reject(ERROR_ASSET_NOT_LOADED + " - " + audioId);
            }
        } catch (Exception ex) {
            call.reject(ex.getMessage());
        }
    }

    @PluginMethod
    public void stop(PluginCall call) {
        try {
            initSoundPool();
            String audioId = call.getString(ASSET_ID);

            if (audioAssetList.containsKey(audioId)) {
                AudioAsset asset = audioAssetList.get(audioId);
                if (asset != null) {
                    asset.stop();
                    call.resolve();
                }
            } else {
                call.reject(ERROR_ASSET_NOT_LOADED + " - " + audioId);
            }
        } catch (Exception ex) {
            call.reject(ex.getMessage());
        }
    }

    @PluginMethod
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
                        call.resolve(status);
                    } else {
                        status = new JSObject();
                        status.put("status", false);
                        call.resolve(status);
                    }
                } else {
                    status = new JSObject();
                    status.put("status", ERROR_AUDIO_ASSET_MISSING + " - " + audioId);
                    call.resolve(status);
                }
            } else {
                status = new JSObject();
                status.put("status", ERROR_AUDIO_ID_MISSING);
                call.resolve(status);
            }
        } catch (Exception ex) {
            call.reject(ex.getMessage());
        }
    }

    @PluginMethod
    public void setVolume(PluginCall call) {
        try {
            initSoundPool();

            String audioId = call.getString(ASSET_ID);
            float volume = call.getFloat(VOLUME);

            if (audioAssetList.containsKey(audioId)) {
                AudioAsset asset = audioAssetList.get(audioId);
                if (asset != null) {
                    asset.setVolume(volume);
                    call.resolve();
                }
            } else {
                call.reject(ERROR_AUDIO_ASSET_MISSING);
            }
        } catch (Exception ex) {
            call.reject(ex.getMessage());
        }
    }

    @PluginMethod
    public void isPlaying(final PluginCall call) {
        try {
            initSoundPool();

            String audioId = call.getString(ASSET_ID);

            if (!isStringValid(audioId)) {
                call.reject(ERROR_AUDIO_ID_MISSING + " - " + audioId);
                return;
            }

            if (audioAssetList.containsKey(audioId)) {
                AudioAsset asset = audioAssetList.get(audioId);
                if (asset != null) {
                    call.resolve(new JSObject().put("isPlaying", asset.isPlaying()));
                }
            } else {
                call.reject(ERROR_AUDIO_ASSET_MISSING + " - " + audioId);
            }
        } catch (Exception ex) {
            call.reject(ex.getMessage());
        }
    }

    public void dispatchComplete(String assetId) {
        JSObject ret = new JSObject();
        ret.put("assetId", assetId);
        notifyListeners("complete", ret);
    }

    private void preloadAsset(PluginCall call) {
        double volume = call.getDouble(VOLUME, 1.0);
        int audioChannelNum = call.getInt(AUDIO_CHANNEL_NUM, 1);

        try {
            initSoundPool();

            String audioId = call.getString(ASSET_ID);

            boolean isUrl = call.getBoolean("isUrl", false);

            if (!isStringValid(audioId)) {
                call.reject(ERROR_AUDIO_ID_MISSING + " - " + audioId);
                return;
            }

            if (!audioAssetList.containsKey(audioId)) {
                String assetPath = call.getString(ASSET_PATH);

                if (!isStringValid(assetPath)) {
                    call.reject(ERROR_ASSET_PATH_MISSING + " - " + audioId + " - " + assetPath);
                    return;
                }

                String fullPath = assetPath; //"raw/".concat(assetPath);

                AssetFileDescriptor assetFileDescriptor;
                if (isUrl) {
                    File f = new File(new URI(fullPath));
                    ParcelFileDescriptor p = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
                    assetFileDescriptor = new AssetFileDescriptor(p, 0, -1);
                } else {
                    if (fullPath.startsWith("content")) {
                        assetFileDescriptor = getBridge()
                            .getActivity()
                            .getContentResolver()
                            .openAssetFileDescriptor(Uri.parse(fullPath), "r");
                    } else {
                        Context ctx = getBridge().getActivity().getApplicationContext();
                        AssetManager am = ctx.getResources().getAssets();
                        assetFileDescriptor = am.openFd(fullPath);
                    }
                }

                AudioAsset asset = new AudioAsset(this, audioId, assetFileDescriptor, audioChannelNum, (float) volume);
                audioAssetList.put(audioId, asset);

                JSObject status = new JSObject();
                status.put("STATUS", "OK");
                call.resolve(status);
            } else {
                call.reject(ERROR_AUDIO_EXISTS);
            }
        } catch (Exception ex) {
            call.reject(ex.getMessage());
        }
    }

    private void playOrLoop(String action, final PluginCall call) {
        try {
            initSoundPool();

            final String audioId = call.getString(ASSET_ID);
            final Double time = call.getDouble("time", 0.0);
            if (audioAssetList.containsKey(audioId)) {
                AudioAsset asset = audioAssetList.get(audioId);
                if (LOOP.equals(action) && asset != null) {
                    asset.loop();
                } else if (asset != null) {
                    asset.play(time, () -> {
                        call.resolve();
                        return null;
                    });
                }
            }
        } catch (Exception ex) {
            call.reject(ex.getMessage());
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
