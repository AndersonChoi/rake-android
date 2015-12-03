package com.rake.android.rkmetrics.metric.model;

import com.rake.android.rkmetrics.util.Logger;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import org.json.JSONObject;

/**
 * action:status 에서 `action` 이 EMPTY 일 경우를 모델링
 */
public class EmptyMetric extends Body {

    public EmptyMetric() {}

    @Override
    public JSONObject toJSONObject() {
        RakeClientMetricSentinelShuttle shuttle = getEmptyShuttle();

        if (null == shuttle) {
            Logger.e("NULL shuttle returned from getEmptyShuttle()");
            return null;
        }

        if (null != header) header.fillShuttle(shuttle);
        fillCommonBodyFields(shuttle);

        return shuttle.toJSONObject();
    }

    @Override
    public String getMetricType() {
        String status = null;
        if (null != header) status = header.getStatus();

        return ":" + status;
    }


}
