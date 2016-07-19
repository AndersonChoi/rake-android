// 키를 포함하고 있어서 공개하지 않습니다.
#define RAKE_LIB_VERSION @""
#define RAKE_LIB_BUILD_DATE @""
#define BUILD_BRANCH @""
#define METRIC_TOKEN_DEV @""
#define METRIC_TOKEN_LIVE @""
#define DEV_SERVER_URL @""
#define LIVE_SERVER_URL @""

#define CRASHLOGGER_TOKEN_DEV @""
#define CRASHLOGGER_TOKEN_LIVE @""

// if you want to add crashreport you have to combine PLCarshReporter static lib with Rake static lib please check TARGET->Framework->Build Phase->script
// 회사 사정으로 CRASHReporter 제거.
//#define USE_PLCRASHREPORTER
