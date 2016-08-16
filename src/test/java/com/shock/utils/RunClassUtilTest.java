package com.shock.utils;

/**
 * Created by shocklee on 16/8/16.
 */
public class RunClassUtilTest {

    public static void main(String[] args) {
        String[][] allGCArguments =
                new String[][] {
                        {                           "-XX:+PrintGCDetails", "-XX:+PrintGCTimeStamps"},
                        {"-XX:+UseSerialGC",        "-XX:+PrintGCDetails", "-XX:+PrintGCTimeStamps"},
                        {"-XX:+UseParallelGC",      "-XX:+PrintGCDetails", "-XX:+PrintGCTimeStamps"},
                        {"-XX:+UseConcMarkSweepGC", "-XX:+PrintGCDetails", "-XX:+PrintGCTimeStamps"},
                        {"-XX:+UseG1GC",            "-XX:+PrintGCDetails", "-XX:+PrintGCTimeStamps"}
                };
        for (String[] gcArguments : allGCArguments)
            RunClassUtil.runClass(RunClass.class,gcArguments ,null);
    }


    public static class RunClass{
        public static void main(String[] args) {
            System.out.println("RunClass====> main");
        }
    }
}
