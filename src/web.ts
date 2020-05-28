import { WebPlugin } from '@capacitor/core';
import { NativeAudioPlugin } from './definitions';

export class NativeAudioWeb extends WebPlugin implements NativeAudioPlugin {
  constructor() {
    super({
      name: 'NativeAudio',
      platforms: ['web']
    });
  }
  configure(options: import("./definitions").ConfigureOptions): Promise<void> {
    throw new Error("Method not implemented.");
  }
  preloadSimple(options: import("./definitions").PreloadSimpleOptions): Promise<void> {
    throw new Error("Method not implemented.");
  }
  preloadComplex(options: import("./definitions").PreloadComplexOptions): Promise<void> {
    throw new Error("Method not implemented.");
  }
  play(options: { assetId: string; }): Promise<void> {
    throw new Error("Method not implemented.");
  }
  loop(options: { assetId: string; }): Promise<void> {
    throw new Error("Method not implemented.");
  }
  stop(options: { assetId: string; }): Promise<void> {
    throw new Error("Method not implemented.");
  }
  unload(options: { assetId: string; }): Promise<void> {
    throw new Error("Method not implemented.");
  }
  setVolumeForComplex(options: { assetId: string; }): Promise<void> {
    throw new Error("Method not implemented.");
  }
}

const NativeAudio = new NativeAudioWeb();

export { NativeAudio };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(NativeAudio);
