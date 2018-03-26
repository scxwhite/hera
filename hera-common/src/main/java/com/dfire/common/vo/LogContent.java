package com.dfire.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 23:24 2018/1/12
 * @desc 任务运行过程中的日志记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogContent {

    private int lines = 0;
    private StringBuffer content = new StringBuffer();

    public void appendConsole(String log) {
        if (lines < 20000) {
            lines++;
            if (log.toLowerCase().contains("error")
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
            if (lines == 20000) {
                content.append("ZEUS# 控制台输出信息过多，停止记录，建议您优化自己的Job");
            }
        }
    }

    public void appendZeus(String log) {
        lines++;
        content.append("ZEUS# ").append(log).append("\n");
    }

    public void append(String log) {
        lines++;
        content.append(log).append("\n");
    }

    public void appendZeusException(Exception e) {
        if (e == null) {
            return;
        }
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        appendZeus(sw.toString());
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
