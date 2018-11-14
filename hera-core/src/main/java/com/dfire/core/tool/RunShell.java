package com.dfire.core.tool;

import com.dfire.core.config.HeraGlobalEnvironment;
import lombok.Data;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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
        commands = new ArrayList<>(3);
        commands.add("sh");
        commands.add("-c");
        commands.add(command);
    }

    public Integer run() {
        builder = new ProcessBuilder(commands);
        builder.directory(new File(directory));
        builder.environment().putAll(HeraGlobalEnvironment.userEnvMap);

        try {
            process = builder.start();
            exitCode = process.waitFor();
            return exitCode;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
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
        return result.toString().trim();
    }

    public static void main(String[] args) throws IOException {
        RunShell shell = new RunShell("sh x.sh");
        shell.setDirectory("/Users/qqr/Downloads/");
        shell.run();
        System.out.println(shell.getResult());
    }
}
