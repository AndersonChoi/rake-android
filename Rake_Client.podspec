Pod::Spec.new do |s|
  s.name         = "Rake_Client"
  s.version      = "1.7.6"
  s.summary      = "Log Tracker for iPhone"
  s.homepage     = "https://github.com/sentinel-skp/rake-iphone"
  s.license      = 'Apache License, Version 2.0'
  s.author       = { "Sentinel @Rake" => "sentinelskp@gmail.com" }
  s.platform     = :ios, '6.0'
  s.source       = { :git => "https://github.com/sentinel-rake/rake-iphone.git", :tag => "r0.5.0_c#{s.version}" }
  s.source_files  = 'Rake/**/*.{m,h}'
  s.private_header_files =  'Rake/Library/**/*.h'
  s.frameworks = 'Foundation', 'SystemConfiguration', 'CoreTelephony'
  s.requires_arc = true
end
