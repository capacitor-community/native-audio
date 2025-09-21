import type { PluginListenerHandle } from '@capacitor/core';

export enum AudioFocusMode {
  /** Allow mixed audio, no focus management */
  NONE = 'none',
  /** Take exclusive audio focus, pause other audio */
  EXCLUSIVE = 'exclusive',
  /** Take audio focus but duck (lower volume) other audio */
  DUCK = 'duck',
}

export interface NativeAudio {
  /**
   * Configure plugin behavior for audio focus and fading
   *
   * @param options Configuration options
   *
   * @example
   * ```typescript
   * // Duck other audio when playing
   * await NativeAudio.configure({
   *   audioFocusMode: AudioFocusMode.DUCK
   * });
   *
   * // Take exclusive focus with fade effect
   * await NativeAudio.configure({
   *   fade: true,
   *   audioFocusMode: AudioFocusMode.EXCLUSIVE
   * });
   * ```
   *
   * @since 1.0.0
   */
  configure(options: ConfigureOptions): Promise<void>;
  preload(options: PreloadOptions): Promise<void>;
  play(options: { assetId: string; time?: number }): Promise<void>;
  pause(options: { assetId: string }): Promise<void>;
  resume(options: { assetId: string }): Promise<void>;
  loop(options: { assetId: string }): Promise<void>;
  stop(options: { assetId: string }): Promise<void>;
  unload(options: { assetId: string }): Promise<void>;
  setVolume(options: { assetId: string; volume: number }): Promise<void>;
  getCurrentTime(options: { assetId: string }): Promise<{ currentTime: number }>;
  getDuration(options: { assetId: string }): Promise<{ duration: number }>;
  isPlaying(options: { assetId: string }): Promise<{ isPlaying: boolean }>;
  /**
   * Listen for asset completed playing event
   *
   * @since 5.0.1
   */
  addListener(eventName: 'complete', listenerFunc: (event: { assetId: string }) => void): Promise<PluginListenerHandle>;
}

export interface ConfigureOptions {
  /**
   * Audio fade configuration
   * @default false
   */
  fade?: boolean;

  /**
   * Audio focus behavior mode
   * @default AudioFocusMode.NONE
   */
  audioFocusMode?: AudioFocusMode;
}

export interface PreloadOptions {
  assetPath: string;
  assetId: string;
  volume?: number;
  audioChannelNum?: number;
  isUrl?: boolean;
}
