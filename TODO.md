# 0.3.18 


5. 앱이 백그라운드로 들어가는 `OnStop()` 에서도 `flush` 를 호출하길 권장합니다. 만약 퍼포먼스가 문제라면, 최소한 앱이 소멸되는 `OnDestory()` 에서는 **반드시** `flush` 를 호출해야합니다. 이는 그래야만 로그가 제때 전송되어, 분석 시점에서 오차를 줄일 수 있기 때문입니다.

- update frequency -> convert TimerTask
- remove handler lock
- convert Handler to HandlerThread
- remove RakeMessageDelegator
- remove cleanEvent message
- move flushCount, url into HandlerThread
- Localize problem
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
- ~~log tag~~
- ~~debug 시 로깅 부분에서 if guard 제거~~

# Next

- Thread -> Rx 로 교체
- getInstance 테스팅 작성
