package com.dfire.core.job;

import java.io.*;

/**
 * Created by xiaosuda on 2018/7/11.
 */
public class ProcessJobTest {

    public static void main(String[] args) throws IOException, InterruptedException {


        String[] commands = {"sudo","-u","pjx","sh","/opt/logs/spring-boot/2018-07-16/debug-480/tmp.sh"};
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
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
                System.out.println(threadName);
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
