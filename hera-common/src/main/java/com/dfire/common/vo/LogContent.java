package com.dfire.common.vo;

import com.dfire.common.constants.Constants;
import com.dfire.common.enums.StatusEnum;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

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

    private final String CONSOLE = "<b>CONSOLE#</b> ";
    private final String HERA = "<b>HERA#</b> ";
    private StringBuffer content;

    private static final int COUNT = 10000;
    private static final int MAIL_PRINT_COUNT = 500;
    private static final String ERROR = "error";


    public void appendConsole(String log) {

        if (lines < COUNT) {
            //空日志不记录
            if (StringUtils.isBlank(log)) {
                return ;
            }
            if (log.toLowerCase().contains(ERROR)
                    || log.toLowerCase().contains(StatusEnum.FAILED.toString())
                    || log.contains("Exception")
                    || log.contains("NullPointException")
                    || log.contains("No such file or directory")
                    || log.contains("command not found")
                    || log.contains("Permission denied")) {
                content.append(CONSOLE).append("<font style=\"color:red\">")
                        .append(log).append("</font>")
                        .append(Constants.LOG_SPLIT);
            } else {
                content.append(CONSOLE).append(log).append(Constants.LOG_SPLIT);
            }
            if (++lines >= COUNT) {
                content.append(HERA).append("控制台输出信息过多，停止记录，建议您优化自己的Job");
            }
        }
    }

    public void appendHera(String log) {
        lines++;
        if (content == null) {
            content = new StringBuffer();
        }
        if (lines < COUNT) {
            content.append(HERA).append(log).append(Constants.LOG_SPLIT);
        }
    }

    public void append(String log) {
        lines++;
        if (content == null) {
            content = new StringBuffer();
        }
        if (lines < COUNT) {
            content.append(HERA).append(log).append(Constants.LOG_SPLIT);
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
        return content.toString();
    }
    
    /**
     * 输出倒数500行信息
     * @return
     */
	public String getMailContent() {
		String c = content.toString().replace(Constants.HTML_NEW_LINE+Constants.HTML_NEW_LINE, Constants.HTML_NEW_LINE);
//    	String[] cs = c.split(Constants.HTML_NEW_LINE);
//    	String re=null;
//    	if(cs.length<=MAIL_PRINT_COUNT){
//    		re=c;
//    	}
//    	else{
//    		StringBuffer sb = null;
//    		for (int i = cs.length-MAIL_PRINT_COUNT; i < cs.length; i++) {
//				sb.append(cs[i]).append(Constants.HTML_NEW_LINE);
//			}
//    		re=sb.toString();
//    	}
    	
        return c;
    }

    public int getLines() {
        return lines;
    }

}
