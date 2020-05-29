import AVFoundation
import Capacitor
import CoreAudio

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitor.ionicframework.com/docs/plugins/ios
 */
@objc(NativeAudio)
public class NativeAudio: CAPPlugin {
    
    var audioList: [String : AnyObject] = [:]
    var fadeMusic = false
    
    public override init() {
        super.init()
        
        fadeMusic = false
        
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
        preloadAsset(call)
    }
    
    @objc func preloadComplex(_ call: CAPPluginCall) {
        preloadAsset(call)
    }
    
    @objc func play(_ call: CAPPluginCall) {
        let audioId = call.getString(Constant.AssetIdKey) ?? ""
        
        if audioId != "" {
            let queue = DispatchQueue(label: "com.getcapacitor.audio.complex.queue", qos: .userInitiated)
            
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
                        } else if (asset as! NSNumber).boolValue {
                            let audioAsset = asset as? NSNumber ?? 0
                            
                            AudioServicesPlaySystemSound(SystemSoundID(truncating: audioAsset))
                            
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
                    
                    call.success()
                }
            } else {
                call.error(Constant.ErrorAssetNotFound)
            }
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
    
    @objc func unload(_ call: CAPPluginCall) {}
    
    @objc func setVoume(_ call: CAPPluginCall) {}
    
    private func preloadAsset(_ call: CAPPluginCall) {
        let audioId = call.getString(Constant.AssetIdKey) ?? ""
        
        if audioId != "" {
            let assetPath = call.getString(Constant.AssetPathKey) ?? ""
            
            if audioList.isEmpty {
                audioList = [:]
            }
            
            let asset = audioList[audioId]
            let queue = DispatchQueue(label: "com.getcapacitor.audio.simple.queue", qos: .userInitiated)
            
            queue.async {
                if asset == nil {
                    let basePath = Bundle.main.url(forAuxiliaryExecutable: assetPath)
                    
                    let path = basePath?.path ?? ""
                    let pathWWW = basePath?.path ?? ""
                    
                    if FileManager.default.fileExists(atPath: path) {
                        let pathUrl = URL(fileURLWithPath: path)
                        let soundFileUrl: CFURL = CFBridgingRetain(pathUrl) as! CFURL
                        var soundId = SystemSoundID()
                        
                        AudioServicesCreateSystemSoundID(soundFileUrl, &soundId)
                        self.audioList[audioId] = NSNumber(value: Int32(soundId))
                        
                        call.success()
                    } else if FileManager.default.fileExists(atPath: pathWWW) {
                        let pathUrl = URL(fileURLWithPath: path)
                        let soundFileUrl: CFURL = CFBridgingRetain(pathUrl) as! CFURL
                        var soundId = SystemSoundID()
                        
                        AudioServicesCreateSystemSoundID(soundFileUrl, &soundId)
                        self.audioList[audioId] = NSNumber(value: Int32(soundId))
                        
                        call.success()
                    } else {
                        call.error(Constant.ErrorAssetPath)
                    }
                }
            }
        }
    }
}
