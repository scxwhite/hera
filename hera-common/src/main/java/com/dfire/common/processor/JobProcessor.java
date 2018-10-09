package com.dfire.common.processor;


import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:45 2018/1/11
 * @desc 可以将其他Job作为一个Processor处理单元，嵌入到当前Job中
 */
@Builder
@Data
public class JobProcessor implements Processor {

    public static final String name = "JobProcessor";

    private String jobId;
    private Map<String, String> kvConfig;

    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getConfig() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("actionId", jobId);
        JSONObject kv = new JSONObject();
        if (kvConfig != null) {
            kvConfig.keySet()
                    .stream()
                    .filter(key -> key.startsWith("instance."))
                    .forEach(value -> kv.put(value, kvConfig.get(value)));
        }
        jsonObject.put("kvConfig", kv);
        return jsonObject.toString();
    }

    @Override
    public void parse(String config) {
        JSONObject jsonObject = JSONObject.parseObject(config);
        jobId = jsonObject.getString("actionId");
        kvConfig = jsonObject.getJSONObject("kvConfig").entrySet().stream()
                             .filter(ks -> jsonObject.getString(ks.toString()) != null)
                             .collect(Collectors.toMap(k -> k.toString(), v -> jsonObject.getString(v.toString()), (s, t) -> t));

    }
}
