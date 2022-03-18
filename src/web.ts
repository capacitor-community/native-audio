import { WebPlugin } from '@capacitor/core';



import { NativeAudio } from "./definitions";
import type {
  ConfigureOptions,
  PreloadOptions,
} from "./definitions";

export class NativeAudioWeb extends WebPlugin implements NativeAudio {
  constructor() {
    super({
      name: "NativeAudio",
      platforms: ["web"],
    });
  }
  resume (options: { assetId: string; }): Promise<void> {
    console.log(options)
    throw new Error('Method not implemented.');
  }
  pause (options: { assetId: string; }): Promise<void> {
    console.log(options)
    throw new Error('Method not implemented.');
  }
  getCurrentTime (options: { assetId: string; time: number }): Promise<{ currentTime: number; }> {
    console.log(options)
    throw new Error('Method not implemented.');
  }
  getDuration (options: { assetId: string; }): Promise<{ duration: number; }> {
    console.log(options);
    throw new Error('Method not implemented.');
  }
  configure(options: ConfigureOptions): Promise<void> {
    console.log(options);
    throw new Error("Method not implemented.");
  }
  preload(options: PreloadOptions): Promise<void> {
    console.log(options);
    throw new Error("Method not implemented.");
  }
  play(options: { assetId: string }): Promise<void> {
    console.log(options);
    throw new Error("Method not implemented.");
  }
  loop(options: { assetId: string }): Promise<void> {
    console.log(options);
    throw new Error("Method not implemented.");
  }
  stop(options: { assetId: string }): Promise<void> {
    console.log(options);
    throw new Error("Method not implemented.");
  }
  unload(options: { assetId: string }): Promise<void> {
    console.log(options);
    throw new Error("Method not implemented.");
  }
  setVolume(options: { assetId: string }): Promise<void> {
    console.log(options);
    throw new Error("Method not implemented.");
  }
  isPlaying(options: { assetId: string }): Promise<{ isPlaying: boolean; }> {
    console.log(options)
    throw new Error('Method not implemented.');
  }
}

const NativeAudio = new NativeAudioWeb();

export { NativeAudio };

