package com.dfire.common.vo;

import com.dfire.common.constants.Constants;
import com.dfire.common.enums.StatusEnum;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.Queue;

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
    
    private LinkedList<String> q ;
    
    
    
    /**
     * size大小的队列
     * @param log
     * @param size
     */
    public void queuePushlog(String log){
    	if(q==null)
    		q= new LinkedList<String>();
    	q.add(log);
    	if(q.size()>=TAIL_PRINT_COUNT){
    		q.removeFirst();
    	}
    }
    
    public String queueToString(){
    	if(lines >= COUNT){
        	StringBuffer sb=new StringBuffer();
        	String[] a = q.toArray(new String[0]);
        	for (int i = 0; i < a.length ; i++) {
    			sb.append(a[i]);
    		}
        	return sb.toString();
    	}
    	else{
    		return "";
    	}

    }


    public void appendConsole(String log) {
    	lines++;
        if (lines < COUNT) {
            //空日志不记录
            if (StringUtils.isBlank(log)) {
                return ;
            }
            content.append(CONSOLE).append(redColorMsg(log)).append(Constants.HTML_NEW_LINE);
            if (lines +1 >= COUNT) {
                content.append(HERA).append("控制台输出信息过多，停止记录，建议您优化自己的Job"+Constants.HTML_NEW_LINE);
                content.append(HERA).append("..."+Constants.HTML_NEW_LINE);
                content.append(HERA).append("..."+Constants.HTML_NEW_LINE);
            }
        }else{
        	queuePushlog(CONSOLE+redColorMsg(log)+Constants.HTML_NEW_LINE);
        }
    }

    public void appendHera(String log) {
        lines++;
        if (content == null) {
            content = new StringBuffer();
        }
        if (lines < COUNT) {
            content.append(HERA).append(log).append(Constants.HTML_NEW_LINE);
        }
        else{
        	queuePushlog(CONSOLE+log+Constants.HTML_NEW_LINE);
        }
    }

    public void append(String log) {
        lines++;
        if (content == null) {
            content = new StringBuffer();
        }
        if (lines < COUNT) {
            content.append(HERA).append(log).append(Constants.HTML_NEW_LINE);
        }else{
        	queuePushlog(CONSOLE+log+Constants.HTML_NEW_LINE);
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
        return content.toString()+Constants.HTML_NEW_LINE+queueToString();
    }
    

	public String getMailContent() {
		String c = getContent().replace(Constants.LOG_SPLIT , Constants.HTML_NEW_LINE);
        return c;
    }

    public String redColorMsg(String log){
    	if (log.toLowerCase().contains(ERROR)
                || log.toLowerCase().contains(StatusEnum.FAILED.toString())
                || log.contains("Exception")
                || log.contains("NullPointException")
                || log.contains("No such file or directory")
                || log.contains("command not found")
                || log.contains("Permission denied")) {
            return "<font style=\"color:red\">"+log+"</font>";
        } else {
        	return log;
        }
    }
	
	
    public int getLines() {
        return lines;
    }

}
