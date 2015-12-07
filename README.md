# Rake-Android

[![Build Status](https://travis-ci.org/skpdi/rake-android.svg?branch=develop)](https://travis-ci.org/skpdi/rake-android) [![Coverage Status](https://coveralls.io/repos/skpdi/rake-android/badge.svg?branch=develop)](https://coveralls.io/r/skpdi/rake-android?branch=develop)

**Rake** 는 단말에서 서버로 로그를 *쉽고*, *안전하게* 전송할 수 있도록 도와주는 *가벼운* 라이브러리입니다. 자세한 내용은 위키를 참조해주세요.

- [Rake Android: Getting Started](https://github.com/skpdi/rake-document/wiki/1.-Rake-Android)
- [Rake Android: App Example](https://github.com/sentinel-rake/rake-android-example)
- [Rake Android: API](http://skpdi.github.io/rake-android/docs/SNAPSHOT/com/rake/android/rkmetrics/RakeAPI.html)

# Development

## Build

빌드시 터미널에 `METRIC_TOKEN_LIVE` 값과 `METRIC_TOKEN_DEV` 값을 입력

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

# License

**Apache V2** 를 따릅니다. 이는 개발에 사용된 [Mixpanel](https://github.com/mixpanel) 이 Apache V2 를 따르기 때문입니다. 라이센스에 따라, 이를 사용하는 앱에서는 라이브러리 사용 여부를 표시해야 하기 때문에 라이센스 전문을 링크합니다.

- [Apache V2](http://www.apache.org/licenses/LICENSE-2.0.html)
- [Mixpanel: Android](https://github.com/mixpanel/mixpanel-android/blob/master/LICENSE)
- [JSnappy (for prior to Rake 0.3)](https://code.google.com/p/jsnappy/source/browse/trunk/LICENCE.txt)

