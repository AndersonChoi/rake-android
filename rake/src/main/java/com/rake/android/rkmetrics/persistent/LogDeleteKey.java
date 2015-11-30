package com.rake.android.rkmetrics.persistent;

/**
 * TOKEN / URL 별로 데이터가 전송되므로, 전송 후 각 단위마다 삭제할 로그를 검색하기 위한 키를 담고 있는 객체
 */
public final class LogDeleteKey {

    private String lastId;
    private String token;
    private String url;

    public String getLastId() { return lastId; }
    public String getToken() { return token; }
    public String getUrl() { return url; }

    private LogDeleteKey() {}
    private LogDeleteKey(String lastId, String token, String url) {
        this.lastId = lastId;
        this.token = token;
        this.url = url;
    }

    public static LogDeleteKey create(String lastId, String token, String url) {
        if (null == lastId || null == token || null == url) return null;

        return new LogDeleteKey(lastId, token, url);
    }
}
