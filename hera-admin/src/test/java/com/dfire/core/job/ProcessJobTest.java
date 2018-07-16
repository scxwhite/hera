package com.dfire.core.job;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

/**
 * Created by xiaosuda on 2018/7/11.
 */
public class ProcessJobTest {

    public static void main(String[] args) throws IOException, InterruptedException {


        ProcessBuilder processBuilder = new ProcessBuilder("ls");
        processBuilder.directory(new File("/"));
        Process process = processBuilder.start();

        StreamThread thread = new StreamThread(process.getInputStream(), "task");
        thread.start();
        process.waitFor();
        System.out.println("结束任务");

    }

    private static class StreamThread extends Thread {
        private InputStream inputStream;
        private String threadName;

        public StreamThread(InputStream inputStream, String threadName) {
            this.inputStream = inputStream;
            this.threadName = threadName;
        }

        @Override
        public void run() {
            try {

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
