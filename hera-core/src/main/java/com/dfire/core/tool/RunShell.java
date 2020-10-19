package com.dfire.core.tool;

import com.dfire.config.HeraGlobalEnv;
import com.dfire.logs.ErrorLog;
import lombok.Data;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaosuda
 * @date 2018/8/6
 */
@Data
public class RunShell {
    private List<String> commands;
    private ProcessBuilder builder;
    private Integer exitCode = -1;
    private Process process;
    private String directory = "/tmp";

    public RunShell(String command) {
        setCommand(command);
    }

    public RunShell() {

    }

    public void setCommand(String command) {
        commands = new ArrayList<>(3);
        commands.add("sh");
        commands.add("-c");
        commands.add(command);
    }

    public Integer run() {
        builder = new ProcessBuilder(commands);
        builder.directory(new File(directory));
        builder.environment().putAll(HeraGlobalEnv.userEnvMap);
        try {
            process = builder.start();
            if (process.waitFor(2, TimeUnit.SECONDS)) {
                return exitCode = 0;
            }
            return exitCode;
        } catch (IOException | InterruptedException e) {
            ErrorLog.error("执行shell异常", e);
        }
        return exitCode;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getResult() throws IOException {
        if (exitCode == 0) {
            return readFromInputStream(process.getInputStream());
        } else {
            return readFromInputStream(process.getErrorStream());
        }
    }

    private String readFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = input.readLine()) != null) {
            result.append(line).append("\n");
        }
        input.close();
        return result.toString().trim();
    }

}
