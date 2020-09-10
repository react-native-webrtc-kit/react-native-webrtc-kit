require 'json'

package = JSON.parse(File.read(File.join(__dir__, '/package.json')))

Pod::Spec.new do |s|
  s.name         = "ReactNativeWebRTCKit"
  s.version      = package['version']
  s.summary      = package['description']
  s.description  = package['description']
  s.homepage     = "https://github.com/react-native-webrtc-kit/react-native-webrtc-kit"
  s.license      = package['license']
  s.author       = { "Shiguredo Inc." => "info@shiguredo.jp" }
  s.platform     = :ios, "10.0"
  s.source       = { :git => "https://github.com/react-native-webrtc-kit/react-native-webrtc-kit.git", :tag => "develop" }
  s.source_files = "ios/*.{h,m}"
  s.requires_arc = true
  
  s.dependency "React"
  s.dependency "WebRTC", "~>  86.4240.1.2" # source 'https://github.com/shiguredo/sora-ios-sdk-specs.git'
end
