/* 아래의 변수들은 컴파일 시작 전 빌드타임에 치환되는 변수들로 변경시 gulpfile.js 도 변경해야 함*/
#define RAKE_LIB_VERSION @"r0.5.0_c1.8.0"
#define RAKE_LIB_BUILD_DATE @"2015-12-15 15:16:38"
#define BUILD_BRANCH @"master"
#define METRIC_TOKEN_DEV @""
#define METRIC_TOKEN_LIVE @""
#define DEV_SERVER_URL @""
#define LIVE_SERVER_URL @""

#define CRASHLOGGER_TOKEN_DEV @""
#define CRASHLOGGER_TOKEN_LIVE @""

// if you want to add crashreport you have to combine PLCarshReporter static lib with Rake static lib please check TARGET->Framework->Build Phase->script
// 회사 사정으로 CRASHReporter 제거.
//#define USE_PLCRASHREPORTER
