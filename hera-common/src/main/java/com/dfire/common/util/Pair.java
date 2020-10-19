package com.dfire.common.util;

/**
 * desc:
 *
 * @author scx
 * @create 2019/07/22
 */
public class Pair<A, B> {

    private A fst;

    private B snd;


    public Pair(A a, B b) {
        this.fst = a;
        this.snd = b;
    }

    public A fst() {
        return fst;
    }

    public B snd() {
        return snd;
    }

    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }
}