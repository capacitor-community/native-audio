import { NativeAudio } from './web';

export class Test {
  /**
   * @param assetId - some string to identify asset
   * @param assetPath - path to ressource like `assets/sounds/bla.mp3`
   */
  public start(assetId: string, assetPath: string): void {
    this.runLater(
      new TestCase('preload', () => NativeAudio.preload({ assetId: assetId, assetPath: assetPath })),
        new TestCase('duration:', async () => console.log(JSON.stringify(await NativeAudio.getDuration({ assetId: assetId })))),
        new TestCase('currentTime:', async () => console.log(JSON.stringify(await NativeAudio.getCurrentTime({ assetId: assetId })))),
        new TestCase('play (0)', () => NativeAudio.play({ assetId: assetId, time: 0 })),
        new TestCase('pause', () => NativeAudio.pause({ assetId: assetId })),
        new TestCase('resume', () => NativeAudio.resume({ assetId: assetId })),
        new TestCase('pause', () => NativeAudio.pause({ assetId: assetId })),
        new TestCase('currentTime:', async () => console.log(JSON.stringify(await NativeAudio.getCurrentTime({ assetId: assetId })))),
        new TestCase('play (0)', () => NativeAudio.play({ assetId: assetId, time: 0 })),
        new TestCase('play (10)', () => NativeAudio.play({ assetId: assetId, time: 10 })),
        new TestCase('stop', () => NativeAudio.stop({ assetId: assetId })),
        new TestCase('loop', () => NativeAudio.loop({ assetId: assetId })),
        new TestCase('setVolume', () => NativeAudio.setVolume({ assetId: assetId, volume: 0.5 })),
        new TestCase('unload', () => NativeAudio.unload({ assetId: assetId })),
        new TestCase('-- FINISH --', () => undefined)
    );
  }

  private runLater(...testCases: Array<TestCase>): void {
    for (let i: number = 0; i < testCases.length; i++) {
      const testCase: TestCase = testCases[i];
      setTimeout(async () => {
        console.log(testCase.fncName);
        await testCase.fnc();
      }, 2000 * i);
    }
  }
}

class TestCase {
  constructor(public readonly fncName: string, public readonly fnc: () => void | Promise<void>) { }
}
