package com.dfire.util;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * desc:
 *
 * @author scx
 * @create 2019/03/27
 */
public class ParseAirflowJob {

    private String jobPath = "/Users/scx/Desktop/tuya_tmpl.txt";

    @Before
    public void init() {

    }

    @Test
    public void parseScript() {
        File file = new File(jobPath);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;
            StringBuilder script = new StringBuilder();
            Set<String> scripts = new HashSet<>();
            while ((line = br.readLine()) != null) {
                script.append(line).append("\n");
                if (line.equals("eeooff")) {
                    scripts.add(script.toString());
                    script = new StringBuilder();
                }
            }
            StringBuilder sql = new StringBuilder("insert into hera_job (description,group_id,name,owner,run_type,script,host_group_id,area_id,repeat_run) values ");
            Set<String> dagIdSet = new HashSet<>();
            for (String s : scripts) {
                String[] split = s.split("123456789987654321");
                if (split.length != 2) {
                    throw new RuntimeException("解析异常:" + s);
                }
                dagIdSet.add(split[0]);
                String[] dagNames = split[0].split(" ");
                if (dagNames.length != 2) {
                    throw new RuntimeException("解析异常:" + s);
                }

                sql.append("(").append(dagNames[0]).append(".").append(dagNames[1]).append(",");
                sql.append(1).append(",");
                sql.append(dagNames[1]).append(",");
                sql.append("docker").append(",");
                sql.append("shell").append(",");
                sql.append(split[1]).append(",");
                sql.append(1).append(",");
                sql.append(1).append(",");
                sql.append(0).append("),");
            }
             System.out.println(sql.toString());

            System.out.println(dagIdSet.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}