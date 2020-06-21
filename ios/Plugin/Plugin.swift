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
    
    var audioList: [String : Any] = [:]
    var fadeMusic = false
    
    public override func load() {
        super.load()
        
        self.fadeMusic = false
        
        do {
            let session = AVAudioSession.sharedInstance()
            try session.setCategory(AVAudioSession.Category.playback)
            try session.setActive(false)
        } catch {
            print("Failed to ")
        }
    }
    
    @objc func configure(_ call: CAPPluginCall) {
        let fade: Bool = call.getBool(Constant.FadeKey) ?? false
        
        fadeMusic = fade
    }
    
    @objc func preloadSimple(_ call: CAPPluginCall) {
        preloadAsset(call, isComplex: false)
    }
    
    @objc func preloadComplex(_ call: CAPPluginCall) {
        preloadAsset(call, isComplex: true)
    }
    
    @objc func play(_ call: CAPPluginCall) {
        let audioId = call.getString(Constant.AssetIdKey) ?? ""
        
        if audioId != "" {
            let queue = DispatchQueue(label: "com.getcapacitor.community.audio.complex.queue", qos: .userInitiated)
            
            queue.async {
                if self.audioList.count > 0 {
                    let asset = self.audioList[audioId]
                    
                    if asset != nil {
                        if asset is AudioAsset {
                            let audioAsset = asset as? AudioAsset
                            
                            if self.fadeMusic {
                                audioAsset?.playWithFade()
                            } else {
                                audioAsset?.play()
                            }
                            
                            call.success()
                        } else if (asset is Int32) {
                            let audioAsset = asset as? NSNumber ?? 0
                            
                            AudioServicesPlaySystemSound(SystemSoundID(audioAsset.intValue ))
                            
                            call.success()
                        } else {
                            call.error(Constant.ErrorAssetNotFound)
                        }
                    }
                }
            }
        }
    }
    
    @objc func resume(_ call: CAPPluginCall) {
//        let audioId = call.getString(Constant.AssetIdKey) ?? ""
        call.success()
    }
    
    @objc func pause(_ call: CAPPluginCall) {
        call.success() // TODO: Implement pause
    }
    
    @objc func stop(_ call: CAPPluginCall) {
        let audioId = call.getString(Constant.AssetIdKey) ?? ""
        
        do {
            try stopAudio(audioId: audioId)
        } catch {
            call.error(Constant.ErrorAssetNotFound)
        }
    }
    
    @objc func loop(_ call: CAPPluginCall) {
        let audioId = call.getString(Constant.AssetIdKey) ?? ""
        
        if self.audioList.count > 0 {
            let asset = self.audioList[audioId]
            
            if asset != nil {
                if asset is AudioAsset {
                    let audioAsset = asset as? AudioAsset
                    audioAsset?.loop()
                    
                    call.success()
                }
            } else {
                call.error(Constant.ErrorAssetNotFound)
            }
        }
    }
    
    @objc func unload(_ call: CAPPluginCall) {
        let audioId = call.getString(Constant.AssetIdKey) ?? ""
        
        do {
            try stopAudio(audioId: audioId)
        } catch {
            call.error(Constant.ErrorAssetNotFound)
        }
        
        let player : AVAudioPlayer! = self.audioList[audioId] as? AVAudioPlayer
        if player != nil {
            player.stop()
        }
    }
    
    @objc func setVoume(_ call: CAPPluginCall) {
        let audioId = call.getString(Constant.AssetIdKey) ?? ""
        let volume = call.getFloat(Constant.Volume) ?? 1.0
        
        let player : AVAudioPlayer! = self.audioList[audioId] as? AVAudioPlayer
        if player != nil {
            player.setVolume(volume, fadeDuration: 1)
        }
    }
    
    private func preloadAsset(_ call: CAPPluginCall, isComplex complex: Bool) {
        let audioId = call.getString(Constant.AssetIdKey) ?? ""
        let channels: NSNumber?
        let volume: Float?
        let delay: NSNumber?
        
        if audioId != "" {
            let assetPath: String = call.getString(Constant.AssetPathKey) ?? ""
            
            if (complex) {
                volume = call.getFloat("volume") ?? 1.0
                channels = NSNumber(value: call.getInt("channels") ?? 1)
                delay = NSNumber(value: call.getInt("delay") ?? 1)
            } else {
                channels = 0
                volume = 0
                delay = 0
            }
            
            if audioList.isEmpty {
                audioList = [:]
            }
            
            let asset = audioList[audioId]
            let queue = DispatchQueue(label: "com.getcapacitor.community.audio.simple.queue", qos: .userInitiated)
            
            queue.async {
                if asset == nil {
                    let assetPathSplit = assetPath.components(separatedBy: ".")
                    let basePath = Bundle.main.path(forResource: assetPathSplit[0], ofType: assetPathSplit[1])
                    
                    if FileManager.default.fileExists(atPath: basePath ?? "") {
                        if !complex {
                            let pathUrl = URL(fileURLWithPath: basePath ?? "")
                            let soundFileUrl: CFURL = CFBridgingRetain(pathUrl) as! CFURL
                            var soundId = SystemSoundID()
                            
                            AudioServicesCreateSystemSoundID(soundFileUrl, &soundId)
                            self.audioList[audioId] = NSNumber(value: Int32(soundId))
                            
                            call.success()
                        } else {
                            let audioAsset: AudioAsset = AudioAsset(path: basePath, withChannels: channels, withVolume: volume as NSNumber?, withFadeDelay: delay)
                            
                            self.audioList[audioId] = audioAsset
                            
                            call.success()
                        }
                    } else {
                        call.error(Constant.ErrorAssetPath)
                    }
                }
            }
        }
    }
    
    private func stopAudio(audioId: String) throws {
        if self.audioList.count > 0 {
            let asset = self.audioList[audioId]
            
            if asset != nil {
                if asset is AudioAsset {
                    let audioAsset = asset as? AudioAsset
                    
                    if self.fadeMusic {
                        audioAsset?.playWithFade()
                    } else {
                        audioAsset?.stop()
                    }
                }
            } else {
                throw MyError.runtimeError(Constant.ErrorAssetNotFound)
            }
        }
    }
}
