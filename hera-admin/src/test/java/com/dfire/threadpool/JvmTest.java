package com.dfire.threadpool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by xiaosuda on 2018/12/17.
 */
public class JvmTest {

    private static final int _1MB = 100 * 1024 * 1024;


    public static void main(String[] args) {
        List<String> list = new ArrayList<>();

        list.add("hello");
        list.add("world");


        List<String[]> strings = list.stream().map(word -> word.split("")).distinct().collect(Collectors.toList());

        for (String[] string : strings) {
            for (String s : string) {
                System.out.print(s + " ");
            }
            System.out.println();
        }


        List<String> collect = list.stream().flatMap(word -> Arrays.stream(word.split(""))).distinct().collect(Collectors.toList());


        for (String s : collect) {
            System.out.print(s + " ");

        }
        System.out.println();

    }

}
