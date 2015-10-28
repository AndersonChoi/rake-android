# 0.3.21

- remove nested map
- Token URL

# 0.3.20

- ~~git rev-parse current branch and replace TOKENs in Token.java~~
- mUlti INstance
- Builder pattern (+ autoflush disable option)
- Shuttle Metric

## API 변경

- setFlushInterval 에서 첫번째 인자인 Context 제거 
- getFlushInterval 추가
- RakeAPI 클래스가 final 클래스로 변경
- 0.3.17 부터 deprecated 되었던 생성자 public static RakeAPI getInstance(Context context, String token, Boolean isDevServer) 를 제거
- 0.3.17 부터 depreacted 되었던 생성자 public static void setDebug(Boolean debug) {
- remove context from setRakeServer(Context context, String server)
- setRakeServer -> setEndpoint

## Design 변경

- Database version update 4 -> 5
- events -> log 테이블


GetInstance(TOKEN, LoggingMode)

- ~~즉시 플러시 시에 오류~~
- env static set 에 넣고 다른거 들어오면 illegal

- 테스트 케이스 만들기 1. 다른 url 2. 즉시 플러시시 오류

- ~~android studio project 로 변경~~

- update frequency -> convert TimerTask
- remove handler lock
- convert Handler to HandlerThread
- remove RakeMessageDelegator
- remove cleanEvent message
- move flushCount, url into HandlerThread
- remove context from RakeAPI instancesMap
- FLUSH, FLUSH_FULL, FLUSH_SCHEDULED 구분
- apply RakeProtocolV2

잘못된 토큰이 1개고, 올바른 토큰이 여러개일 경우에 response 가 -1 이 떨어짐

```java
rakeLive = RakeAPI.getInstance(this, "a" /*올바른 라이브 토큰*/, false);
rakeDev = RakeAPI.getInstance(this, "b", /*잘못된 라이브 토큰*/false);

rakeLive.track(shuttle.toJSONObject());
rakeLive.track(shuttle.toJSONObject());
rakeDev.track(shuttle.toJSONObject());
```

장기적으로는 SQLite 구분자에 token, url 이 들어가야 할듯

8553, 8443, setRakeServer 의 경우에도 둘 중 나중에 초기화 된 것으로 사용되는 문제가 있을듯


# 0.3.19

- HttpUrlConnection support
- Locale.US

# 0.3.18 

- ~~Https Security Problem~~
    
# 0.3.17

- ~~singleton handler ~~
- ~~new constructor using enum~~
- ~~flush interval static scope~~
- ~~remove application context from RakeAPI~~
- ~~IllegarArgumentException 주석~~

- ~~multiple instance 일 경우 setUrl 금지~~

- ~~gradle copy jar~~
- ~~track 함수 null defence~~
- ~~post 메소드 NPE 처리~~
- ~~post 메소드 gzip 헤더 제거~~
- ~~statusCode 500 일 경우 
- ~~json tag~~
- ~~debug 시 로깅 부분에서 if guard 제거~~

# Next

- Thread -> Rx 로 교체
- getInstance 테스팅 작성
