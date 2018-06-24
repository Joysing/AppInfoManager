package com.example.joysing.myapplication;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {

        String a="hello";
        String b="he"+"llo";
        String c=new String ("hello");
        System.out.print(a.equals(c));
    }
}