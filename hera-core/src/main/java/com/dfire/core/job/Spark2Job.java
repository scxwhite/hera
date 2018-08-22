package com.dfire.core.job;

import com.dfire.common.constants.RunningJobKeyConstant;
import com.dfire.common.service.HeraFileService;
import com.dfire.core.tool.ConnectionTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * @Description : Thrift spark job
 * @Author ： HeGuoZi
 * @Date ： 14:02 2018/8/22
 * @Modified :
 */
@Slf4j
public class Spark2Job extends ProcessJob {

    private HeraFileService    heraFileService;
    private ApplicationContext applicationContext;

    private final int maxOutputNum = 2000;

    public Spark2Job(JobContext jobContext, ApplicationContext applicationContext) {
        super(jobContext);
        this.applicationContext = applicationContext;
        this.heraFileService = (HeraFileService) this.applicationContext.getBean("heraFileService");
        jobContext.getProperties().setProperty(RunningJobKeyConstant.JOB_RUN_TYPE, "Spark2Job");
    }

    @Override
    public int run() throws Exception {
        return runInner();
    }

    private Integer runInner() throws Exception {
        String script = getProperties().getLocalProperty(RunningJobKeyConstant.JOB_SCRIPT);
        int last = 0;
        for (int now = 0; now < script.length() && last < script.length(); now++) {
            if (last <= now - 1) {
                if (";".equals(script.substring(now, now + 1))) {
                    executeAndPrint(script, last, now);
                    last = now + 1;
                } else if (now == script.length() - 1) {
                    executeAndPrint(script, last, now + 1);
                    break;
                }
            }
        }
        return 0;
    }

    private void executeAndPrint(String script, int startPoint, int endPoint) {
        try {
            Statement stmt = ConnectionTool.getConnection();
            ResultSet resultSet = stmt.executeQuery(script.substring(startPoint, endPoint));
            int columnCount = resultSet.getMetaData().getColumnCount();
            int rowNum = 0;
            while (resultSet.next()) {
                String line = "";
                for (int i = 1; i <= columnCount; i++) {
                    line += resultSet.getString(i) + " ";
                }
                logConsole(line);
                rowNum++;
                if (rowNum == maxOutputNum) {
                    logConsole("The part of rows exceed " + maxOutputNum + ", won't be displayed");
                    break;
                }
            }
            resultSet.close();
        } catch (Exception e) {
            e.printStackTrace();
            log("执行或打印结果错误");
        }
    }

    @Override
    public List<String> getCommandList() {
        //该方法此处无用(不会被调到)
        return null;
    }
}
