package com.dfire.threadpool;

/**
 * Created by xiaosuda on 2018/12/17.
 */
public class JvmTest {

    private static final int _1MB = 100 * 1024 * 1024;


    public static void main(String[] args) throws InterruptedException {

        ThreadLocal<byte[]> local = new ThreadLocal<>();

        local.set(new byte[_1MB]);

        local = null;
        System.gc();
        ThreadLocal<Integer> local2 = new ThreadLocal<>();

        local2.set(1);
        System.gc();


    }

}
