
  Pod::Spec.new do |s|
    s.name = 'NativeAudio'
    s.version = '0.0.1'
    s.summary = 'Capacitor Native Audio'
    s.license = 'MIT'
    s.homepage = 'git@github.com:capacitor-community:native-audio.git'
    s.author = 'Priyank Patel <priyank.patel@stackspace.ca>'
    s.source = { :git => 'git@github.com:capacitor-community:native-audio.git', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end