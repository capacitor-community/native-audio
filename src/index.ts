import { registerPlugin } from '@capacitor/core';

import { NativeAudio } from './definitions';

const NativeAudio = registerPlugin<NativeAudio>('NativeAudio', {
  web: () => import('./web').then((m) => new m.NativeAudioWeb()),
});

export * from './definitions';
export { NativeAudio };
