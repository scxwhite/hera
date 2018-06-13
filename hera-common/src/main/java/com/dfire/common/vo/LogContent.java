package com.dfire.common.vo;

import lombok.Builder;
import lombok.Data;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 23:24 2018/1/12
 * @desc 任务运行过程中的日志记录
 */
@Data
@Builder
public class LogContent {

    private int lines;
    private StringBuffer content;

    private static final int COUNT = 20000;
    private static final String ERROR = "error";

    public void appendConsole(String log) {
        System.out.println("脚本执行结果" + log);
        if (lines < COUNT) {
            lines++;
            if (log.toLowerCase().contains(ERROR)
                    || log.toLowerCase().contains("failed")
                    || log.contains("FileNotFoundException")
                    || log.contains("NullPointException")
                    || log.contains("No such file or directory")
                    || log.contains("command not found")
                    || log.contains("Permission denied")) {
                content.append("CONSOLE# ").append("<font style=\"color:red\">")
                        .append(log).append("</font>")
                        .append("\n");
            } else {
                content.append("CONSOLE# ").append(log).append("\n");
            }
            if (lines == COUNT) {
                content.append("HERA# 控制台输出信息过多，停止记录，建议您优化自己的Job");
            }
        }
    }

    public void appendHera(String log) {
        lines++;
        if (content == null) {
            content = new StringBuffer();
        }
        content.append("HERA# ").append(log).append("\n");
    }

    public void append(String log) {
        lines++;
        if (content == null) {
            content = new StringBuffer();
        }
        content.append(log).append("\n");
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
        return content.toString();
    }

    public int getLines() {
        return lines;
    }

}
