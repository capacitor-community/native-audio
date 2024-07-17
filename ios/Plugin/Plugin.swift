import AVFoundation
import Foundation
import Capacitor
import CoreAudio

enum MyError: Error {
    case runtimeError(String)
}

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitor.ionicframework.com/docs/plugins/ios
 */
@objc(NativeAudio)
public class NativeAudio: CAPPlugin {

    private var audioList: [String: Any] = [:]
    private let lock = NSLock()
    var fadeMusic = false
    var session = AVAudioSession.sharedInstance()

    override public func load() {
        super.load()

        self.fadeMusic = false

        do {
            try self.session.setCategory(AVAudioSession.Category.playback)
            try self.session.setActive(false)
        } catch {
            print("Failed to set session category")
        }
    }

    @objc func configure(_ call: CAPPluginCall) {
        if let fade = call.getBool(Constant.FadeKey) {
            self.fadeMusic = fade
        }
        if let focus = call.getBool(Constant.FocusAudio) {
            do {
                if focus {
                    try self.session.setCategory(AVAudioSession.Category.playback)
                } else {
                    try self.session.setCategory(AVAudioSession.Category.ambient)
                }
            } catch {
                print("Failed to set setCategory audio")
            }
        }
    }

    @objc func preload(_ call: CAPPluginCall) {
        preloadAsset(call, isComplex: true)
    }

    @objc func play(_ call: CAPPluginCall) {
        let audioId = call.getString(Constant.AssetIdKey) ?? ""
        let time = call.getDouble("time") ?? 0
        if audioId != "" {
            DispatchQueue.global(qos: .userInitiated).async {
                self.lock.lock()
                defer { self.lock.unlock() }

                if let asset = self.audioList[audioId] {
                    if let audioAsset = asset as? AudioAsset {
                        if self.fadeMusic {
                            audioAsset.playWithFade(time: time)
                        } else {
                            audioAsset.play(time: time)
                        }
                        call.resolve()
                    } else if let audioAsset = asset as? NSNumber {
                        AudioServicesPlaySystemSound(SystemSoundID(audioAsset.intValue))
                        call.resolve()
                    } else {
                        call.reject(Constant.ErrorAssetNotFound)
                    }
                }
            }
        }
    }

    @objc private func getAudioAsset(_ call: CAPPluginCall) -> AudioAsset? {
        let audioId = call.getString(Constant.AssetIdKey) ?? ""
        if audioId.isEmpty {
            call.reject(Constant.ErrorAssetId)
            return nil
        }

        lock.lock()
        defer { lock.unlock() }

        if let asset = self.audioList[audioId], asset is AudioAsset {
            return asset as? AudioAsset
        }

        call.reject(Constant.ErrorAssetNotFound + " - " + audioId)
        return nil
    }

    @objc func getDuration(_ call: CAPPluginCall) {
        guard let audioAsset = self.getAudioAsset(call) else {
            return
        }

        call.resolve(["duration": audioAsset.getDuration()])
    }

    @objc func getCurrentTime(_ call: CAPPluginCall) {
        guard let audioAsset = self.getAudioAsset(call) else {
            return
        }

        call.resolve(["currentTime": audioAsset.getCurrentTime()])
    }

    @objc func resume(_ call: CAPPluginCall) {
        guard let audioAsset = self.getAudioAsset(call) else {
            return
        }

        audioAsset.resume()
        call.resolve()
    }

    @objc func pause(_ call: CAPPluginCall) {
        guard let audioAsset = self.getAudioAsset(call) else {
            return
        }

        audioAsset.pause()
        call.resolve()
    }

    @objc func stop(_ call: CAPPluginCall) {
        let audioId = call.getString(Constant.AssetIdKey) ?? ""

        do {
            try stopAudio(audioId: audioId)
            call.resolve()
        } catch {
            call.reject(Constant.ErrorAssetNotFound)
        }
    }

    @objc func loop(_ call: CAPPluginCall) {
        guard let audioAsset = self.getAudioAsset(call) else {
            return
        }

        audioAsset.loop()
        call.resolve()
    }

    @objc func unload(_ call: CAPPluginCall) {
        let audioId = call.getString(Constant.AssetIdKey) ?? ""
        lock.lock()
        defer { lock.unlock() }

        if let asset = self.audioList[audioId], asset is AudioAsset {
            (asset as? AudioAsset)?.unload()
            self.audioList[audioId] = nil
        }

        call.resolve()
    }

    @objc func setVolume(_ call: CAPPluginCall) {
        guard let audioAsset = self.getAudioAsset(call) else {
            return
        }

        let volume = call.getFloat(Constant.Volume) ?? 1.0

        audioAsset.setVolume(volume: volume as NSNumber)
        call.resolve()
    }

    @objc func isPlaying(_ call: CAPPluginCall) {
        guard let audioAsset = self.getAudioAsset(call) else {
            return
        }

        call.resolve(["isPlaying": audioAsset.isPlaying()])
    }

    private func preloadAsset(_ call: CAPPluginCall, isComplex complex: Bool) {
        let audioId = call.getString(Constant.AssetIdKey) ?? ""
        guard !audioId.isEmpty else { return }

        let channels: NSNumber?
        let volume: Float?
        let delay: NSNumber?
        let isUrl: Bool?

        let assetPath: String = call.getString(Constant.AssetPathKey) ?? ""

        if complex {
            volume = call.getFloat("volume") ?? 1.0
            channels = NSNumber(value: call.getInt("channels") ?? 1)
            delay = NSNumber(value: call.getInt("delay") ?? 1)
            isUrl = call.getBool("isUrl") ?? false
        } else {
            channels = 0
            volume = 0
            delay = 0
            isUrl = false
        }

        DispatchQueue.global(qos: .userInitiated).async {
            self.lock.lock()
            defer { self.lock.unlock() }

            if self.audioList[audioId] == nil {
                var basePath: String?
                if isUrl == false {
                    let assetPathSplit = assetPath.components(separatedBy: ".")
                    basePath = Bundle.main.path(forResource: assetPathSplit[0], ofType: assetPathSplit[1])
                } else {
                    let url = URL(string: assetPath)
                    basePath = url?.path
                }

                if let basePath = basePath, FileManager.default.fileExists(atPath: basePath) {
                    if !complex {
                        let pathUrl = URL(fileURLWithPath: basePath)
                        let soundFileUrl: CFURL = pathUrl as CFURL
                        var soundId = SystemSoundID()
                        AudioServicesCreateSystemSoundID(soundFileUrl, &soundId)
                        self.audioList[audioId] = NSNumber(value: Int32(soundId))
                        call.resolve()
                    } else {
                        let audioAsset = AudioAsset(owner: self, withAssetId: audioId, withPath: basePath, withChannels: channels, withVolume: volume as NSNumber?, withFadeDelay: delay)
                        self.audioList[audioId] = audioAsset
                        call.resolve()
                    }
                } else {
                    call.reject(Constant.ErrorAssetPath + " - " + assetPath)
                }
            } else {
                call.reject(Constant.ErrorAssetExists)
            }
        }
    }

    private func stopAudio(audioId: String) throws {
        lock.lock()
        defer { lock.unlock() }

        if let asset = self.audioList[audioId] {
            if let audioAsset = asset as? AudioAsset {
                if self.fadeMusic {
                    audioAsset.playWithFade(time: audioAsset.getCurrentTime())
                } else {
                    audioAsset.stop()
                }
            }
        } else {
            throw MyError.runtimeError(Constant.ErrorAssetNotFound)
        }
    }
}
