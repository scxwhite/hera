package com.dfire.common.vo;

import com.dfire.common.constants.Constants;
import com.dfire.common.enums.StatusEnum;
import com.dfire.config.HeraGlobalEnv;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 23:24 2018/1/12
 * @desc 任务运行过程中的日志记录
 */
@Data
public class LogContent {

    private int lines;

    private final String CONSOLE = "<b>CONSOLE#</b> ";
    private final String HERA = "<b>HERA#</b> ";
    private StringBuffer content;

    private static final int HEAD_PRINT_COUNT = HeraGlobalEnv.getWebLogHeadCount();

    private static final int TAIL_PRINT_COUNT = HeraGlobalEnv.getWebLogTailCount();

    private boolean limit = true;

    private final String limitLog = "控制台输出信息过多,停止记录,建议您优化自己的Job" + Constants.LOG_SPLIT;


    private static final String ERROR = "error";

    private LinkedList<String> tailLog;

    private Lock lock;

    public LogContent() {
        this.tailLog = new LinkedList<>();
        this.content = new StringBuffer();
        this.lock = new ReentrantLock();
    }

    public LogContent(StringBuffer content) {
        this.content = content;
        this.tailLog = new LinkedList<>();
        this.lock = new ReentrantLock();
    }


    /**
     * size大小的队列
     *
     * @param log
     */
    private void queuePushLog(String log) {
        try {
            lock.lock();
            tailLog.add(log);
            if (tailLog.size() >= TAIL_PRINT_COUNT) {
                tailLog.removeFirst();
            }
        } finally {
            lock.unlock();
        }
    }

    private String tailLog() {
        if (lines >= HEAD_PRINT_COUNT) {
            StringBuilder sb = new StringBuilder();
            String[] tailLogs;
            try {
                lock.lock();
                tailLogs = tailLog.toArray(new String[0]);
            } finally {
                lock.unlock();
            }
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
        if (lines < HEAD_PRINT_COUNT) {
            content.append(CONSOLE).append(redColorMsg(log)).append(Constants.LOG_SPLIT);
            appendLimitLog();
        } else {
            queuePushLog(CONSOLE + redColorMsg(log) + Constants.LOG_SPLIT);
        }
    }


    public void appendHera(String log) {
    	String[] l = log.split(Constants.NEW_LINE);
    	for (String s : l) {
            if (lines < HEAD_PRINT_COUNT) {
                content.append(HERA).append(s).append(Constants.LOG_SPLIT);
                appendLimitLog();
            } else {
                queuePushLog(HERA + s + Constants.LOG_SPLIT);
            }
		}
    }

    public void append(String log) {
        if (lines < HEAD_PRINT_COUNT) {
            content.append(log).append(Constants.LOG_SPLIT);
            appendLimitLog();
        } else {
            queuePushLog(log + Constants.LOG_SPLIT);
        }
    }


    private void appendLimitLog() {
        if (++lines == HEAD_PRINT_COUNT) {
            content.append(HERA).append(limitLog);
            content.append(HERA).append(limitLog);
            content.append(HERA).append(limitLog);
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
