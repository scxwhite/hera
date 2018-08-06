package com.dfire.core.tool;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaosuda
 * @date 2018/8/6
 */
@Slf4j
@Data
public class RunShell {
    private List<String> commands;
    private ProcessBuilder builder;
    private Integer exitCode = -1;
    private Process process;

    public RunShell(String command) {
        commands = new ArrayList<>(3);
        commands.add("sh");
        commands.add("-c");
        commands.add(command);
    }

    public Integer run() {
        builder = new ProcessBuilder(commands);
        try {
            process = builder.start();
            exitCode = process.waitFor();
            return exitCode;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return exitCode;
    }

    public String getResult() throws IOException {
        if (exitCode == 0) {
            return readFromInputStream(process.getInputStream());
        } else {
            log.error(readFromInputStream(process.getErrorStream()));
        }
        return null;
    }

    private String readFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = input.readLine()) != null) {
            result.append(line).append("\n");
        }
        return result.toString().trim();
    }
}
