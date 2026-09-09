#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'flutter_barcode_scanner_plus'
  s.version          = '3.0.13'
  s.summary          = 'An enhanced Flutter plugin for barcode scanning on Android and iOS.'
  s.description      = <<-DESC
An enhanced Flutter plugin for barcode scanning on Android and iOS with additional features.
                       DESC
  s.homepage         = 'https://github.com/SarangthemAmarjit/flutter_barcode_scanner_plus'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Sarangthem Amarjit Meetei' => 'sarangthemamarjit123@gmail.com' }
  s.source           = { :path => '.' }
  s.source_files     = 'Classes/**/*.{swift,h,m}'
  s.public_header_files = 'Classes/**/*.h'
  s.resources        = 'Assets/*.png'
  s.dependency       'Flutter'


  s.ios.deployment_target = '12.0'
  s.swift_version = '5.0'
end
