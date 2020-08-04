<p align="center"><br><img src="https://user-images.githubusercontent.com/236501/85893648-1c92e880-b7a8-11ea-926d-95355b8175c7.png" width="128" height="128" /></p>
<h3 align="center">Native Audio</h3>
<p align="center"><strong><code>@capacitor-community/native-audio</code></strong></p>
<p align="center">
  Capacitor community plugin for native audio.
</p>

<p align="center">
  <img src="https://img.shields.io/maintenance/yes/2020?style=flat-square" />
  <a href="https://github.com/capacitor-community/native-audio/actions?query=workflow%3A%22Test+and+Build+Plugin%22"><img src="https://img.shields.io/github/workflow/status/capacitor-community/native-audio/Test%20and%20Build%20Plugin?style=flat-square" /></a>
  <a href="https://www.npmjs.com/package/@capacitor-community/native-audio"><img src="https://img.shields.io/npm/l/@capacitor-community/native-audio?style=flat-square" /></a>
<br>
  <a href="https://www.npmjs.com/package/@capacitor-community/native-audio"><img src="https://img.shields.io/npm/dw/@capacitor-community/native-audio?style=flat-square" /></a>
  <a href="https://www.npmjs.com/package/@capacitor-community/native-audio"><img src="https://img.shields.io/npm/v/@capacitor-community/native-audio?style=flat-square" /></a>
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
<a href="#contributors-"><img src="https://img.shields.io/badge/all%20contributors-1-orange?style=flat-square" /></a>
<!-- ALL-CONTRIBUTORS-BADGE:END -->
</p>

## Maintainers

| Maintainer    | GitHub                                      | Social                                           | Sponsoring Company |
| ------------- | ------------------------------------------- | ------------------------------------------------ | ------------------ |
| Priyank Patel | [priyankpat](https://github.com/priyankpat) | [@priyankpat\_](https://twitter.com/priyankpat_) | Ionic              |

## Installation

To use npm

```bash
npm install @capacitor-community/native-audio
```

To use yarn

```bash
yarn add @capacitor-community/native-audio
```

Sync native files

```bash
npx cap sync
```

On iOS, no further steps are needed.

On Android, register the plugin in your main activity:

```java
import com.getcapacitor.community.audio.nativeaudio.NativeAudio;

public class MainActivity extends BridgeActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initializes the Bridge
    this.init(
        savedInstanceState,
        new ArrayList<Class<? extends Plugin>>() {

          {
            // Additional plugins you've installed go here
            // Ex: add(TotallyAwesomePlugin.class);
            add(NativeAudio.class);
          }
        }
      );
  }
}
```

## Configuration

No configuration required for this plugin.

## Supported methods

| Name           | Android | iOS | Web |
| :------------- | :------ | :-- | :-- |
| preloadSimple  | ✅      | ✅  | ❌  |
| preloadComplex | ✅      | ✅  | ❌  |
| play           | ✅      | ✅  | ❌  |
| pause          | ✅      | ❌  | ❌  |
| resume         | ✅      | ❌  | ❌  |
| loop           | ✅      | ✅  | ❌  |
| stop           | ✅      | ✅  | ❌  |
| unload         | ✅      | ✅  | ❌  |
| setVolume      | ✅      | ✅  | ❌  |
| getDuration    | ✅      | ✅  | ❌  |
| getCurrentTime | ✅      | ✅  | ❌  |
| isLoaded       | ✅      | ✅  | ❌  |

## Usage

```typescript
import { Plugins } from "@capacitor/core";

const { NativeAudio } = Plugins;

/**
 * Platform: Android/iOS
 * This method will load short duration audio file into memory.
 * @param assetPath - relative path of the file in app bundle (iOS) OR name of resource file (no extension) in res/raw (Android)
 *        assetId - unique identifier of the file
 * @returns void
 */
NativeAudio.preloadSimple({
  assetPath: "audio/chime.mp3",
  assetId: "chime_audio",
});

/**
 * Platform: Android/iOS
 * This method will load more optimized audio files for background into memory.
 * @param assetPath - relative path of the file in app bundle (iOS) OR name of resource file (no extension) in res/raw (Android), OR absolute file url (file://) by passing `isUrl=true`
 *        assetId - unique identifier of the file
 *        volume - numerical value of the volume between 0.1 - 1.0
 *        audioChannelNum - number of audio channels
 *        fade - boolean true/false whether to fade transitions
 *        isUrl - pass true if assetPath is a `file://` url
 * @returns void
 */
NativeAudio.preloadComplex({
  assetPath: "audio/inception.mp3",
  assetId: "inception_audio",
  volume: 1.0,
  audioChannelNum: 1,
  isUrl: false
});

/**
 * Platform: Android/iOS
 * This method will play the loaded audio file if present in the memory.
 * @param assetId - identifier of the asset
 * @returns void
 */
NativeAudio.play({
  assetId: "chime_audio",
});

/**
 * Platform: Android/iOS
 * This method will loop the audio file for playback.
 * @param assetId - identifier of the asset
 * @returns void
 */
NativeAudio.loop({
  assetId: "chime_audio",
});

/**
 * Platform: Android/iOS
 * This method will stop the audio file during playback.
 * @param assetId - identifier of the asset
 * @returns void
 */
NativeAudio.stop({
  assetId: "chime_audio",
});

/**
 * Platform: Android/iOS
 * This method will pause the audio file during playback.
 * @param assetId - identifier of the asset
 * @returns void
 */
NativeAudio.pause({
  assetId: "chime_audio",
});

/**
 * Platform: Android/iOS
 * This method will resume the audio file if paused.
 * @param assetId - identifier of the asset
 * @returns void
 */
NativeAudio.resume({
  assetId: "chime_audio",
});

/**
 * Platform: Android/iOS
 * This method will stop and unload the audio file.
 * @param assetId - identifier of the asset
 * @returns void
 */
NativeAudio.unload({
  assetId: "chime_audio",
});

/**
 * Platform: Android/iOS
 * This method will set the new volume for a audio file.
 * @param assetId - identifier of the asset
 *        volume - numerical value of the volume between 0.1 - 1.0
 * @returns void
 */
NativeAudio.setVolume({
  assetId: "chime_audio",
  volume: 0.4,
});

/**
 * this method will get the duration of an audio file.
 * only works if channels == 1
 */
NativeAudio.getDuration({
  assetId: 'inception_audio'
})
.then(result => {
  console.log(result.duration);
})

/**
 * this method will get the current time of a playing audio file.
 * only works if channels == 1
 */
NativeAudio.getCurrentTime({
  assetId: 'inception_audio'
});
.then(result => {
  console.log(result.currentTime);
})
```
