# Rake-iPhone

**Rake** 는 단말에서 서버로 로그를 *쉽고*, *안전하게* 전송할 수 있도록 도와주는 *가벼운* 라이브러리입니다. 자세한 내용은 위키를 참조해주세요.

- [Rake iPhone Manual](https://github.com/sentinel-rake/rake-document/wiki/2.-Rake-iPhone)
- [Rake Objective C  Example](https://github.com/skpdi/rake-iphone/tree/master/rake-iOS-example-Objc)
- [Rake Swift  Example](https://github.com/skpdi/rake-iphone/tree/master/rake-iOS-example-Swift)

##Requirements
- iOS7 이상
- Xcode 6 이상
- Objective C와 Swift 2.x 을 지원합니다.

##Install
###Static Framework
 1. Rake Static Framework 를 다운로드 받으세요
 2. Realm.framework 을 선택하여 Xcode 프로젝트의 File Navigation에 넣습니다. 이때, Copy items if needed 이 선택된지 확인하고, Finish 버튼을 누릅니다.
 3. XCode 프로젝트 ->Tartget -> Build Settings -> Linking -> Other Linker Flags 에 -ObjC를 추가합니다 (Category 지원)

----------


## Developement

### Test

테스트 실행을 위해서는, 먼저 iOS 시뮬레이터를 실행하고 해당 버전에 맞게 테스트 실행  
버전에 맞지 않는 시뮬레이터가 켜져 있을 경우 타임아웃이 발생

```
xcodebuild -workspace iphone.xcworkspace -scheme RakeTests -destination 'OS=9.1,name=iPhone 6s' clean test
```

### Continuous Testing

사용할 시뮬레이터를 실행하고

```
METRIC_TOKEN_DEV= METRIC_TOKEN_LIVE= TEST_TOKEN_DEV= TEST_TOKEN_LIVE= gulp test-continuous --version=1.7.8
```

### Build

```
METRIC_TOKEN_DEV= METRIC_TOKEN_LIVE= TEST_TOKEN_DEV= TEST_TOKEN_LIVE= gulp build --version=1.7.8
```

## License

**Apache V2** 를 따릅니다. 이는 개발에 사용된 [Mixpanel](https://github.com/mixpanel) 이 Apache V2 를 따르기 때문입니다. 라이센스에 따라, 이를 사용하는 앱에서는 라이브러리 사용 여부를 표시해야 하기 때문에 라이센스 전문을 링크합니다.

- [Apache V2](http://www.apache.org/licenses/LICENSE-2.0.html)
- [Mixpanel: iPhone](https://github.com/mixpanel/mixpanel-iphone)
