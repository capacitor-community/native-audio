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

    @objc func preloadSimple(_ call: CAPPluginCall) {
        if !call.hasOption("assetPath") {
            call.error("assetPath property is missing")
            return
        }
        
        if !call.hasOption("assetId") {
            call.error("assetId property is missing")
            return
        }
        
        preloadAsset(call, isComplex: false)
    }
    
    @objc func preloadComplex(_ call: CAPPluginCall) {
        if !call.hasOption("assetPath") {
            call.error("assetPath property is missing")
            return
        }
        
        
        if !call.hasOption("assetId") {
            call.error("assetId property is missing")
            return
        }
        
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
    
    @objc private func getAudioAsset(_ call: CAPPluginCall) -> AudioAsset? {
        let audioId = call.getString(Constant.AssetIdKey) ?? ""
        if audioId == "" {
            call.error(Constant.ErrorAssetId)
            return nil
        }
        if self.audioList.count > 0 {
            let asset = self.audioList[audioId]
            if asset != nil && asset is AudioAsset {
                return asset as? AudioAsset
            }
        }
        call.error(Constant.ErrorAssetNotFound + " - " + audioId)
        return nil
    }
    
    @objc func getDuration(_ call: CAPPluginCall) {
        guard let audioAsset: AudioAsset = self.getAudioAsset(call) else {
            return
        }

        call.resolve([
            "duration": audioAsset.getDuration()
        ])
    }
    
    @objc func getCurrentTime(_ call: CAPPluginCall) {
        guard let audioAsset: AudioAsset = self.getAudioAsset(call) else {
            return
        }
        
        call.resolve([
            "currentTime": audioAsset.getCurrentTime()
        ])
    }

    @objc func isLoaded(_ call: CAPPluginCall) {
        if !call.hasOption("assetId") {
            call.error("assetId property is missing")
            return
        }

        let audioId = call.getString(Constant.AssetIdKey) ?? ""

        call.resolve([
            "isLoaded": self.audioList[audioId] != nil
        ])
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
        guard let audioAsset: AudioAsset = self.getAudioAsset(call) else {
            return
        }
        
        audioAsset.loop()
        call.success()
    }
    
    @objc func unload(_ call: CAPPluginCall) {
        let audioId = call.getString(Constant.AssetIdKey) ?? ""
        if self.audioList.count > 0 {
            let asset = self.audioList[audioId]
            if asset != nil && asset is AudioAsset {
                let audioAsset = asset as! AudioAsset
                audioAsset.unload()
                self.audioList[audioId] = nil
            }
        }
        
        call.success()
    }
    
    @objc func setVolume(_ call: CAPPluginCall) {
        if !call.hasOption(Constant.AssetIdKey) {
            call.error(Constant.AssetIdKey + " property is missing")
            return
        }
        
        let audioId = call.getString(Constant.AssetIdKey) ?? ""
        let volume = call.getFloat(Constant.Volume) ?? 1.0
        
        if self.audioList[audioId] == nil {
            call.error(audioId + " asset is not loaded")
        }
        
        let asset = self.audioList[audioId]
        
        if asset != nil {
            if asset is AudioAsset {
                let audioAsset = asset as? AudioAsset
                audioAsset?.setVolume(volume: NSNumber(value: volume))
                
                call.success()
            }
        }
    }
    
    private func preloadAsset(_ call: CAPPluginCall, isComplex complex: Bool) {
        let audioId = call.getString(Constant.AssetIdKey) ?? ""
        let assetPath: String = call.getString(Constant.AssetPathKey) ?? ""
        let channels: NSNumber?
        let volume: Float?
        let delay: NSNumber?
        let isUrl: Bool?
        
        if call.hasOption("fade") {
            self.fadeMusic = call.getBool("fade") ?? false
        }

        if (complex) {
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

        if audioList.isEmpty {
            audioList = [:]
        }        

        let asset = audioList[audioId]
        let queue = DispatchQueue(label: "com.getcapacitor.community.audio.simple.queue", qos: .userInitiated)
        
        if asset != nil {
            call.error(audioId + " asset is already loaded")
            return
        }
        
        queue.async {
            var basePath: String?
            if isUrl == false {
                let assetPathSplit = assetPath.components(separatedBy: ".")
                basePath = Bundle.main.path(forResource: assetPathSplit[0], ofType: assetPathSplit[1])
            } else {
                let url = URL(string: assetPath)
                basePath = url!.path
            }
            
            if FileManager.default.fileExists(atPath: basePath ?? "") {
                if !complex {
                    let pathUrl = URL(fileURLWithPath: basePath ?? "")
                    let soundFileUrl: CFURL = CFBridgingRetain(pathUrl) as! CFURL
                    var soundId = SystemSoundID()
                    
                    AudioServicesCreateSystemSoundID(soundFileUrl, &soundId)
                    self.audioList[audioId] = NSNumber(value: Int32(soundId))
                    
                    call.success()
                } else {
                    let audioAsset: AudioAsset = AudioAsset(owner: self, withAssetId: audioId, withPath: basePath, withChannels: channels, withVolume: volume as NSNumber?, withFadeDelay: delay)
                    
                    self.audioList[audioId] = audioAsset
                    
                    call.success()
                }
            } else {
                call.error(Constant.ErrorAssetPath + " - " + assetPath)
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
