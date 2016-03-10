# Rake-Android

[![Build Status](https://travis-ci.org/skpdi/rake-android.svg?branch=develop)](https://travis-ci.org/skpdi/rake-android) [![Coverage Status](https://coveralls.io/repos/skpdi/rake-android/badge.svg?branch=develop&service=github)](https://coveralls.io/github/skpdi/rake-android?branch=develop) 

*[Change Log](https://github.com/skpdi/rake-android/blob/master/CHANGELOG.md)*

<br/>

*Rake* 는 안드로이드 앱에서 JSON 로그를 전송할 수 있도록 도와주는 라이브러리입니다.
  
- **no extra dependency** required
- support **android 2.2** (API Level 8)
- **small** (*~100K*)

자세한 내용은 위키를 참조해주세요.

- [Rake Android: Getting Started](https://github.com/skpdi/rake-document/wiki/1.-Rake-Android)
- [Rake Android: App Example](https://github.com/sentinel-rake/rake-android-example)
- [Rake Android: API](http://skpdi.github.io/rake-android/docs/SNAPSHOT/com/rake/android/rkmetrics/RakeAPI.html)

## Supported OS Versions
## Development

### Build

```gradle
> METRIC_TOKEN_LIVE= METRIC_TOKEN_DEV= gradle clean build
```

### Android Studio on OSX

Android Studio 사용시, gradle 이 환경변수를 읽지 못하므로 다음과 같이 터미널에 세팅
 
```
> launchctl setenv METRIC_TOKEN_LIVE {TOKEN}  
> launchctl setenv METRIC_TOKEN_DEV $METRIC_TOKEN_DEV {TOKEN} 
```

추가적으로 테스트 실행을 위해 Android Studio 에서 `run configuration` 환경변수에 다음을 등록

- `METRIC_TOKEN_LIVE`
- `METRIC_TOKEN_ENV`

## License

**Apache 2.0** 

- [Mixpanel: Android](https://github.com/mixpanel/mixpanel-android/blob/master/LICENSE)
- [JSnappy (for prior to Rake 0.3)](https://code.google.com/p/jsnappy/source/browse/trunk/LICENCE.txt)

