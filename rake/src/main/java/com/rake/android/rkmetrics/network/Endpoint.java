package com.rake.android.rkmetrics.network;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;

import com.rake.android.rkmetrics.RakeAPI;
import com.rake.android.rkmetrics.android.SystemInformation;
import com.rake.android.rkmetrics.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Endpoint {
    private static final String AREA_DEFAULT = "korea";
    private static final String AREA_ASIA = "asia";

    private static HashMap<String, Pair<String, String>> uriMap;
    private static HashMap<String, HashSet<String>> areaCountryMap;

    static {
        // Class Loading시에 다음 값들을 초기화한다.

        uriMap = new HashMap<>();

        // default : 대한민국 / 과금 Port
        uriMap.put(
                AREA_DEFAULT,
                new Pair<>(
                        "https://pg.rake.skplanet.com:8443/log/putlog/client",  //Dev
                        "https://rake.skplanet.com:8443/log/putlog/client"      //Live
                )
        );

        // asia 지역 (태국, 인도네시아, 말레이시아 등등)
        uriMap.put(
                AREA_ASIA,
                new Pair<>(
                        "https://pg.asia-rake.skplanet.com/log/putlog/client",  //Dev
                        "https://asia-rake.skplanet.com/log/putlog/client"      //Live
                )
        );

        // TODO 기타 외국 지역 서버가 추가될 경우, uriMap에 Dev/Live 서버 URL을 추가 필요
    }

    static {
        // 국가코드 참고 : https://ko.wikipedia.org/wiki/ISO_3166-1

        areaCountryMap = new HashMap<>();

        // asia 서비스지역 국가 코드 추가 (태국, 말레이시아, 인도네시아, 싱가포르)
        HashSet<String> countrySet = new HashSet<>();
        countrySet.addAll(new ArrayList<>(Arrays.asList(new String[]{"TH", "MY", "ID", "SG"})));
        areaCountryMap.put(AREA_ASIA, countrySet);

        // TODO 기타 외국 지역 서버가 추가될 경우, areaCountryMap에 국가코드 추가 필요
    }

    private String uri;
    private String versionSuffix = "";

    public Endpoint(Context context, RakeAPI.Env env) {
        String area = determineCurrentArea(context);

        setProperURI(area, env);
    }

    private String determineCurrentArea(Context context) {
        if (context == null) {
            return AREA_DEFAULT;
        }

        // 1. USIM Country Code 획득(해외 USIM을 사용하지 않는 이상, 보통 자국의 Country Code가 획득됨)
        String countryCode = SystemInformation.getCountryCodeFromUSIM(context);

        if (TextUtils.isEmpty(countryCode)) {
            // 2. USIM Country Code 획득 실패시, Network Country Code 획득
            countryCode = SystemInformation.getCountryCodeFromNetwork(context);

            if (TextUtils.isEmpty(countryCode)) {
                // 3. Network Country Code 획득 실패시 대안으로 Language Code 획득.
                // 100% 실제 사용자의 소속 국가와 맞진 않음 (예:ES 스페인, 실제 지역은 칠레)
                countryCode = SystemInformation.getLanguageCode(context);
            }
        }

        if (areaCountryMap.get(AREA_ASIA).contains(countryCode)) {
            versionSuffix = "_aws";
            return AREA_ASIA;
        }

        versionSuffix = "";
        return AREA_DEFAULT;
    }

    private void setProperURI(String area, RakeAPI.Env env) {
        Pair<String, String> uriPair = uriMap.get(area);

        if (uriPair == null) {
            uriPair = uriMap.get(AREA_DEFAULT);
        }

        this.uri = (env == RakeAPI.Env.DEV ? uriPair.first : uriPair.second);
    }

    public String getURI() {
        return uri;
    }

    public String getVersionSuffix() {
        return versionSuffix;
    }

    public boolean changeURIPort(int port) {
        Pattern pattern = Pattern.compile(":\\d+/");
        Matcher m = pattern.matcher(uri);

        String portFound = null;
        while (m.find()) {
            portFound = m.group();
        }

        if (portFound == null) {
            Logger.w("no port value in the uri");
            return false;
        }

        uri = uri.replace(portFound, ":" + port + "/");
        return true;
    }
}
