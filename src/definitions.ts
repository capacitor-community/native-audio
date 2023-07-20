export interface NativeAudio {
  configure(options: ConfigureOptions): Promise<void>;
  preload(options: PreloadOptions): Promise<void>;
  play(options: { assetId: string, time: number }): Promise<void>;
  pause(options: { assetId: string }): Promise<void>;
  resume(options: { assetId: string }): Promise<void>;
  loop(options: { assetId: string }): Promise<void>;
  stop(options: { assetId: string }): Promise<void>;
  unload(options: { assetId: string }): Promise<void>;
  setVolume(options: { assetId: string; volume: number }): Promise<void>;
  getCurrentTime(options: {
    assetId: string;
  }): Promise<{ currentTime: number }>;
  getDuration(options: { assetId: string }): Promise<{ duration: number }>;
  isPlaying(options: {
    assetId: string;
  }): Promise<{ isPlaying: boolean }>;
}

export interface ConfigureOptions {
  /**
   * indicating whether or not to fade audio.
   * @default true
   */
  fade?: boolean;
  /**
   * indicating whether or not to disable mixed audio.
   * @default true */
  focus?: boolean;
}

export interface PreloadOptions {
  assetPath: string;
  assetId: string;
  volume?: number;
  audioChannelNum?: number;
  isUrl?: boolean;
}
