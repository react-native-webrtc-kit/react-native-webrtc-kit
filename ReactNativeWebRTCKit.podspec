require 'json'

package = JSON.parse(File.read(File.join(__dir__, '/package.json')))

Pod::Spec.new do |s|
  s.name         = "ReactNativeWebRTCKit"
  s.version      = package['version']
  s.summary      = package['description']
  s.description  = package['description']
  s.homepage     = "https://github.com/shiguredo/react-native-webrtc-kit"
  s.license      = package['license']
  s.author       = { "Shiguredo Inc." => "info@shiguredo.jp" }
  s.platform     = :ios, "12.0"
  s.source       = { :git => "https://github.com/shiguredo/react-native-webrtc-kit.git", :tag => "develop" }
  s.source_files = "ios/*.{h,m}"
  s.requires_arc = true
  
  s.dependency "React"
  s.dependency "WebRTC", "~> 70.17.0" # source 'https://github.com/shiguredo/sora-ios-sdk-specs.git'
end
