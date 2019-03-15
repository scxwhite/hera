package com.dfire.common.vo;

import com.dfire.common.constants.Constants;
import com.dfire.common.enums.StatusEnum;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 23:24 2018/1/12
 * @desc 任务运行过程中的日志记录
 */
@Data
@Builder
public class LogContent {

    private int lines;

    private final String CONSOLE = "<b>CONSOLE#</b> ";
    private final String HERA = "<b>HERA#</b> ";
    private StringBuffer content;

    private static final int COUNT = 8000;
    private static final int TAIL_PRINT_COUNT = 2000;
    private static final String ERROR = "error";

    private LinkedList<String> tailLog;


    /**
     * size大小的队列
     *
     * @param log
     */
    private void queuePushLog(String log) {
        if (tailLog == null) {
            tailLog = new LinkedList<>();
        }
        tailLog.add(log);
        if (tailLog.size() >= TAIL_PRINT_COUNT) {
            tailLog.removeFirst();
        }
    }

    private String tailLog() {
        if (lines >= COUNT) {
            StringBuilder sb = new StringBuilder();
            String[] tailLogs = tailLog.toArray(new String[0]);
            for (String log : tailLogs) {
                sb.append(log);
            }
            return sb.toString();
        } else {
            return "";
        }
    }


    public void appendConsole(String log) {
        //空日志不记录
        if (StringUtils.isBlank(log)) {
            return;
        }
        lines++;
        if (lines < COUNT) {
            content.append(CONSOLE).append(redColorMsg(log)).append(Constants.LOG_SPLIT);
            if (lines + 1 >= COUNT) {
                content.append(HERA).append("控制台输出信息过多，停止记录，建议您优化自己的Job" + Constants.LOG_SPLIT);
                content.append(HERA).append("..." + Constants.LOG_SPLIT);
                content.append(HERA).append("..." + Constants.LOG_SPLIT);
            }
        } else {
            queuePushLog(CONSOLE + redColorMsg(log) + Constants.LOG_SPLIT);
        }
    }

    public void appendHera(String log) {
        lines++;
        if (content == null) {
            content = new StringBuffer();
        }
        if (lines < COUNT) {
            content.append(HERA).append(log).append(Constants.LOG_SPLIT);
        } else {
            queuePushLog(HERA + log + Constants.LOG_SPLIT);
        }
    }

    public void append(String log) {
        lines++;
        if (content == null) {
            content = new StringBuffer();
        }
        if (lines < COUNT) {
            content.append(log).append(Constants.LOG_SPLIT);
        } else {
            queuePushLog(log + Constants.LOG_SPLIT);
        }
    }

    public void appendHeraException(Exception e) {
        if (e == null) {
            return;
        }
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        appendHera(sw.toString());
    }

    public void setContent(StringBuffer content) {
        this.content = content;
    }

    public String getContent() {
        return content.toString() + tailLog();

    }


    public String getMailContent() {
        return getContent();
    }

    private String redColorMsg(String log) {
        if (log.toLowerCase().contains(ERROR)
                || log.toLowerCase().contains(StatusEnum.FAILED.toString())
                || log.contains("Exception")
                || log.contains("NullPointException")
                || log.contains("No such file or directory")
                || log.contains("command not found")
                || log.contains("Permission denied")) {
            return "<font style=\"color:red\">" + log + "</font>";
        } else {
            return log;
        }
    }


    public int getLines() {
        return lines;
    }

}
