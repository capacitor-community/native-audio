declare module "@capacitor/core" {
  interface PluginRegistry {
    NativeAudio: NativeAudioPlugin;
  }
}

export interface NativeAudioPlugin {
  configure(options: ConfigureOptions): Promise<void>;
  preloadSimple(options: PreloadSimpleOptions): Promise<void>;
  preloadComplex(options: PreloadComplexOptions): Promise<void>;
  play(options: { assetId: string; }): Promise<void>;
  loop(options: { assetId: string; }): Promise<void>;
  stop(options: { assetId: string; }): Promise<void>;
  unload(options: { assetId: string; }): Promise<void>;
  setVolume(options: { assetId: string; volume: number; }): Promise<void>;
}

export interface ConfigureOptions {
  fade?: boolean;
}

export interface PreloadSimpleOptions {
  assetPath: string;
  assetId: string;
}

export interface PreloadComplexOptions {
  assetPath: string;
  assetId: string;
  volume?: number;
  audioChannelNum?: number;
}