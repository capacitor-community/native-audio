import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  IonHeader,
  IonButton,
  IonToolbar,
  IonTitle,
  IonContent,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardContent,
} from '@ionic/angular/standalone';

// NATIVE
import { NativeAudio } from '@capacitor-community/native-audio';

@Component({
  selector: 'app-home',
  templateUrl: './home.page.html',
  styleUrls: ['./home.page.scss'],
  standalone: true,
  imports: [
    CommonModule,
    IonButton,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonCard,
    IonCardHeader,
    IonCardTitle,
    IonCardContent,
  ]
})
export class HomePage {

  // Properties to display feedback in the UI
  status = 'Ready';
  duration = 0;
  currentTime = 0;
  playingStatus = false;

  // Asset details
  // IMPORTANT: Make sure you have a file at 'src/assets/sounds/fire.mp3' in your project
  assetId = 'fire';
  // assetPath = 'assets/sounds/fire.mp3';
  assetPath = 'public/assets/sounds/fire.mp3';

  constructor() { }

  /**
   * This method will load a more optimized audio file for background playback into memory.
   */
  public async preload(): Promise<void> {
    this.status = `Preloading ${this.assetId}...`;
    try {
      await NativeAudio.preload({
        assetId: this.assetId,
        assetPath: this.assetPath,
        audioChannelNum: 1,
        isUrl: false
      });
      this.status = 'Preload Complete';
    } catch (e: any) {
      this.status = `Error preloading: ${e.message}`;
    }
  }

  /**
   * This method will play the loaded audio file if present in the memory.
   */
  public async play(): Promise<void> {
    this.status = `Playing ${this.assetId}...`;
    try {
      // The 'time' property is optional to start playback from a specific second.
      await NativeAudio.play({ assetId: this.assetId /*, time: 6.0 */ });
      this.status = 'Playing';
    } catch (e: any) {
      this.status = `Error playing: ${e.message}`;
    }
  }

  /**
   * This method will loop the audio file for playback.
   */
  public async loop(): Promise<void> {
    this.status = `Looping ${this.assetId}...`;
    try {
      await NativeAudio.loop({ assetId: this.assetId });
      this.status = 'Looping';
    } catch (e: any) {
      this.status = `Error looping: ${e.message}`;
    }
  }

  /**
   * This method will stop the audio file if it's currently playing.
   */
  public async stop(): Promise<void> {
    this.status = `Stopping ${this.assetId}...`;
    try {
      await NativeAudio.stop({ assetId: this.assetId });
      this.status = 'Stopped';
    } catch (e: any) {
      this.status = `Error stopping: ${e.message}`;
    }
  }

  /**
   * This method will unload the audio file from the memory.
   */
  public async unload(): Promise<void> {
    this.status = `Unloading ${this.assetId}...`;
    try {
      await NativeAudio.unload({ assetId: this.assetId });
      this.status = 'Unloaded';
      this.duration = 0;
      this.currentTime = 0;
    } catch (e: any) {
      this.status = `Error unloading: ${e.message}`;
    }
  }

  /**
   * This method will set the new volume for an audio file.
   */
  public async setVolume(): Promise<void> {
    const volume = 0.5;
    this.status = `Setting volume to ${volume}...`;
    try {
      await NativeAudio.setVolume({ assetId: this.assetId, volume });
      this.status = `Volume set to ${volume}`;
    } catch (e: any) {
      this.status = `Error setting volume: ${e.message}`;
    }
  }

  /**
   * This method will get the duration of an audio file in seconds.
   */
  public async getDuration(): Promise<void> {
    this.status = 'Getting duration...';
    try {
      const result = await NativeAudio.getDuration({ assetId: this.assetId });
      this.duration = result.duration;
      this.status = `Duration: ${this.duration.toFixed(2)}s`;
    } catch (e: any) {
      this.status = `Error getting duration: ${e.message}`;
    }
  }

  /**
   * This method will get the current playback time of an audio file in seconds.
   */
  public async getCurrentTime(): Promise<void> {
    this.status = 'Getting current time...';
    try {
      const result = await NativeAudio.getCurrentTime({ assetId: this.assetId });
      this.currentTime = result.currentTime;
      this.status = `Current Time: ${this.currentTime.toFixed(2)}s`;
    } catch (e: any) {
      this.status = `Error getting current time: ${e.message}`;
    }
  }

  /**
   * This method will return true if audio is currently playing.
   */
  public async isPlaying(): Promise<void> {
    this.status = 'Checking playing status...';
    try {
      const result = await NativeAudio.isPlaying({ assetId: this.assetId });
      this.playingStatus = result.isPlaying;
      this.status = `Is Playing: ${this.playingStatus}`;
    } catch (e: any) {
      this.status = `Error checking playing status: ${e.message}`;
    }
  }
}

