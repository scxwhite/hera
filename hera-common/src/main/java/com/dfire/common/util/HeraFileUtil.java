package com.dfire.common.util;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:04 2018/4/6
 * @desc
 */
public class HeraFileUtil {



    public static JSONArray parseHeraToJson() {

        JSONArray result = new JSONArray();
        JSONObject head = new JSONObject();
//        head.put("name", "文档中心");
//        head.put("open","true");

        JSONArray childArray = new JSONArray();

        JSONObject personFile = new JSONObject();
        JSONArray personArray = new JSONArray();
        JSONObject person1 = new JSONObject();
        person1.put("name", "个人文件");
        personArray.add(person1);

        personFile.put("name", "个人文档");
        personFile.put("children", personArray.toJSONString());


        JSONObject commonFile = new JSONObject();
        JSONArray commonArray = new JSONArray();
        JSONObject common1 = new JSONObject();
        common1.put("name", "公共文件");
        commonArray.add(common1);

        commonFile.put("name", "公共文档");
        commonFile.put("children", commonArray.toJSONString());

        childArray.add(personFile);
        childArray.add(commonFile);


        head.put("children", childArray.toJSONString());
        result.add(head);
        return result;
    }

    public static void main(String[] args) {
        JSONArray json = parseHeraToJson();
        System.out.println(json.toJSONString());
        String srt = json.toJSONString().replaceAll("\\\\","").replaceAll("\\\"","");
        System.out.println(srt);

    }


}

