package com.getcapacitor.community.audio;

import static com.getcapacitor.community.audio.Constant.ASSET_ID;
import static com.getcapacitor.community.audio.Constant.ASSET_PATH;
import static com.getcapacitor.community.audio.Constant.AUDIO_CHANNEL_NUM;
import static com.getcapacitor.community.audio.Constant.LOOP;
import static com.getcapacitor.community.audio.Constant.VOLUME;

import android.Manifest;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

@NativePlugin(
  permissions = {
    Manifest.permission.MODIFY_AUDIO_SETTINGS,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_PHONE_STATE,
  }
)
public class NativeAudio
  extends Plugin
  implements AudioManager.OnAudioFocusChangeListener {
  public static final String TAG = "NativeAudio";

  private static HashMap<String, AudioAsset> audioAssetList;
  private static ArrayList<AudioAsset> resumeList;

  @Override
  public void load() {
    super.load();

    AudioManager audioManager = (AudioManager) getBridge()
      .getActivity()
      .getSystemService(Context.AUDIO_SERVICE);

    if (audioManager != null) {
      int result = audioManager.requestAudioFocus(
        this,
        AudioManager.STREAM_MUSIC,
        AudioManager.AUDIOFOCUS_GAIN
      );

      initSoundPool();
    }
  }

  @Override
  public void onAudioFocusChange(int focusChange) {
    if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {} else if (
      focusChange == AudioManager.AUDIOFOCUS_GAIN
    ) {} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {}
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
      Log.d(
        TAG,
        "Exception caught while listening for handleOnPause: " +
        ex.getLocalizedMessage()
      );
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
      Log.d(
        TAG,
        "Exception caught while listening for handleOnResume: " +
        ex.getLocalizedMessage()
      );
    }
  }

  /**
   * This method will load short duration audio file into memory.
   * @param call
   */
  @PluginMethod
  public void preloadSimple(final PluginCall call) {
    if (!call.hasOption(ASSET_PATH)) {
      call.error(ASSET_PATH + " property is missing");
      return;
    }

    if (!call.hasOption(ASSET_ID)) {
      call.error(ASSET_ID + " property is missing");
      return;
    }

    new Thread(
      new Runnable() {

        @Override
        public void run() {
          preloadAsset(call);
        }
      }
    )
    .start();
  }

  /**
   * This method will load more optimized audio files for background into memory.
   * @param call
   */
  @PluginMethod
  public void preloadComplex(final PluginCall call) {
    if (!call.hasOption(ASSET_PATH)) {
      call.error(ASSET_PATH + " property is missing");
      return;
    }

    if (!call.hasOption(ASSET_ID)) {
      call.error(ASSET_ID + " property is missing");
      return;
    }

    new Thread(
      new Runnable() {

        @Override
        public void run() {
          preloadAsset(call);
        }
      }
    )
    .start();
  }

  /**
   * This method will play the loaded audio file if present in the memory.
   * @param call
   */
  @PluginMethod
  public void play(final PluginCall call) {
    if (!call.hasOption(ASSET_ID)) {
      call.error(ASSET_ID + " property is missing");
      return;
    }

    getBridge()
      .getActivity()
      .runOnUiThread(
        new Runnable() {

          @Override
          public void run() {
            playOrLoop("play", call);
          }
        }
      );
  }

  /**
   * This method will return the current time of the audio file
   * @param call
   */
  @PluginMethod
  public void getCurrentTime(final PluginCall call) {
    try {
      initSoundPool();

      if (!call.hasOption(ASSET_ID)) {
        call.error(ASSET_ID + " property is missing");
        return;
      }

      String audioId = call.getString(ASSET_ID);

      if (!audioAssetList.containsKey(audioId)) {
        call.error(audioId + " asset is not loaded");
        return;
      }

      AudioAsset asset = audioAssetList.get(audioId);
      if (asset != null) {
        call.success(
          new JSObject().put("currentTime", asset.getCurrentPosition())
        );
      }
    } catch (Exception ex) {
      call.error(ex.getMessage());
    }
  }

  /**
   * This method will return the duration of the audio file
   * @param call
   */
  @PluginMethod
  public void getDuration(final PluginCall call) {
    try {
      initSoundPool();

      if (!call.hasOption(ASSET_ID)) {
        call.error(ASSET_ID + " property is missing");
        return;
      }

      String audioId = call.getString(ASSET_ID);

      if (!audioAssetList.containsKey(audioId)) {
        call.error(audioId + " asset is not loaded");
        return;
      }

      AudioAsset asset = audioAssetList.get(audioId);
      if (asset != null) {
        call.success(new JSObject().put("duration", asset.getDuration()));
      }
    } catch (Exception ex) {
      call.error(ex.getMessage());
    }
  }

  /**
   * This method will return whether an audio file is loaded
   * @param call
   */
  @PluginMethod
  public void isLoaded(final PluginCall call) {
    try {
      initSoundPool();

      if (!call.hasOption(ASSET_ID)) {
        call.error(ASSET_ID + " property is missing");
        return;
      }

      String audioId = call.getString(ASSET_ID);

      call.resolve(
        new JSObject().put("isLoaded", audioAssetList.containsKey(audioId))
      );
    } catch (Exception ex) {
      call.error(ex.getMessage());
    }
  }

  /**
   * This method will loop the audio file for playback.
   * @param call
   */
  @PluginMethod
  public void loop(final PluginCall call) {
    if (!call.hasOption(ASSET_ID)) {
      call.error(ASSET_ID + " property is missing");
      return;
    }

    getBridge()
      .getActivity()
      .runOnUiThread(
        new Runnable() {

          @Override
          public void run() {
            playOrLoop("loop", call);
          }
        }
      );
  }

  /**
   * This method will pause the audio file during playback.
   * @param call
   */
  @PluginMethod
  public void pause(PluginCall call) {
    if (!call.hasOption(ASSET_ID)) {
      call.error(ASSET_ID + " property is missing");
      return;
    }

    try {
      initSoundPool();

      String audioId = call.getString(ASSET_ID);

      if (!audioAssetList.containsKey(audioId)) {
        call.error(audioId + " asset is not loaded");
        return;
      }

      AudioAsset asset = audioAssetList.get(audioId);
      if (asset != null) {
        boolean wasPlaying = asset.pause();

        if (wasPlaying) {
          resumeList.add(asset);
        }
      }
      call.success();
    } catch (Exception ex) {
      call.error(ex.getMessage());
    }
  }

  /**
   * This method will resume the audio file during playback.
   * @param call
   */
  @PluginMethod
  public void resume(PluginCall call) {
    if (!call.hasOption(ASSET_ID)) {
      call.error(ASSET_ID + " property is missing");
      return;
    }

    try {
      initSoundPool();

      String audioId = call.getString(ASSET_ID);

      if (!audioAssetList.containsKey(audioId)) {
        call.error(audioId + " asset is not loaded");
        return;
      }

      AudioAsset asset = audioAssetList.get(audioId);
      if (asset != null) {
        asset.resume();
        resumeList.add(asset);
      }
    } catch (Exception ex) {
      call.error(ex.getMessage());
    }
  }

  /**
   * This method will stop the audio file during playback.
   * @param call
   */
  @PluginMethod
  public void stop(PluginCall call) {
    if (!call.hasOption(ASSET_ID)) {
      call.error(ASSET_ID + " property is missing");
      return;
    }

    try {
      initSoundPool();

      String audioId = call.getString(ASSET_ID);

      if (!audioAssetList.containsKey(audioId)) {
        call.error(audioId + " asset is not loaded");
        return;
      }

      AudioAsset asset = audioAssetList.get(audioId);
      if (asset != null) {
        asset.stop();
      }
    } catch (Exception ex) {
      call.error(ex.getMessage());
    }
  }

  /**
   * This method will stop and unload the audio file.
   * @param call
   */
  @PluginMethod
  public void unload(PluginCall call) {
    if (!call.hasOption(ASSET_ID)) {
      call.error(ASSET_ID + " property is missing");
      return;
    }

    try {
      initSoundPool();

      new JSObject();
      JSObject status;

      String audioId = call.getString(ASSET_ID);

      if (!audioAssetList.containsKey(audioId)) {
        call.error(audioId + " asset is not loaded");
        return;
      }

      AudioAsset asset = audioAssetList.get(audioId);
      if (asset != null) {
        asset.stop();
        asset.unload();
        audioAssetList.remove(audioId);
      }
      call.success();
    } catch (Exception ex) {
      call.error(ex.getMessage());
    }
  }

  /**
   * This method will adjust volume to specified value
   * @param call
   */
  @PluginMethod
  public void setVolume(PluginCall call) {
    if (!call.hasOption(ASSET_ID)) {
      call.error(ASSET_ID + " property is missing");
      return;
    }

    if (!call.hasOption(VOLUME)) {
      call.error(VOLUME + " property is missing");
      return;
    }

    try {
      initSoundPool();

      String audioId = call.getString(ASSET_ID);
      float volume = call.getFloat(VOLUME);

      if (!audioAssetList.containsKey(audioId)) {
        call.error(audioId + " asset is not loaded");
        return;
      }

      AudioAsset asset = audioAssetList.get(audioId);
      if (asset != null) {
        asset.setVolume(volume);
      }
    } catch (Exception ex) {
      call.error(ex.getMessage());
    }
  }

  public void dispatchComplete(String assetId) {
    JSObject ret = new JSObject();
    ret.put("assetId", assetId);
    notifyListeners("complete", ret);
  }

  private void preloadAsset(PluginCall call) {
    double volume = 1.0;
    int audioChannelNum = 1;

    String audioId = call.getString(ASSET_ID);
    String assetPath = call.getString(ASSET_PATH);

    try {
      initSoundPool();

      if (audioAssetList.containsKey(audioId)) {
        call.error(audioId + " asset is already loaded");
        return;
      }

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

      boolean isUrl = call.getBoolean("isUrl", false);
      AssetFileDescriptor assetFileDescriptor;

      if (isUrl) {
        File f = new File(new URI(assetPath));
        ParcelFileDescriptor p = ParcelFileDescriptor.open(
          f,
          ParcelFileDescriptor.MODE_READ_ONLY
        );
        assetFileDescriptor = new AssetFileDescriptor(p, 0, -1);
      } else {
        Context ctx = getBridge().getActivity().getApplicationContext();
        int identifier = ctx
          .getResources()
          .getIdentifier(assetPath, "raw", this.getContext().getPackageName());
        assetFileDescriptor = ctx.getResources().openRawResourceFd(identifier);
      }

      AudioAsset asset = new AudioAsset(
        this,
        audioId,
        assetFileDescriptor,
        audioChannelNum,
        (float) volume
      );
      audioAssetList.put(audioId, asset);

      call.success();
    } catch (Exception exp) {
      if (exp instanceof FileNotFoundException) {
        call.error(assetPath + " file cannot be found");
      } else {
        call.error(exp.getMessage());
      }
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
          asset.play(
            new Callable<Void>() {

              @Override
              public Void call() throws Exception {
                call.success(new JSObject().put(ASSET_ID, audioId));

                return null;
              }
            }
          );
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
}
