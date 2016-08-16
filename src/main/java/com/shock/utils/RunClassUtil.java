package com.shock.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;

/**
 * Created by shocklee on 16/8/16.
 * java执行main函数的方法.
 */
public class RunClassUtil {

    public static void runClass(Class<?> classHasMainMethod, String[] vmArguments, String[] programArguments) {
        try {
            StringBuilder sbVmArguments = new StringBuilder();
            StringBuilder sbProgramArguments = new StringBuilder();

            if (vmArguments != null) {
                for (String arg : vmArguments) {
                    sbVmArguments.append(arg).append(" ");
                }
            }

            if (programArguments != null) {
                for (String arg : programArguments) {
                    sbProgramArguments.append(arg).append(" ");
                }
            }

            String classPath = URLDecoder.decode(RunClassUtil.class.getClassLoader().getResource(".").getPath());
            Process proc =
                    Runtime.getRuntime().exec(
                            "java" + " -cp " + "\"" + classPath + "\"" + " " +
                                    sbVmArguments.toString() + " " +
                                    classHasMainMethod.getName() + " " +
                                    sbProgramArguments.toString());

            BufferedReader outputReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String line = null;

            while ((line = outputReader.readLine()) != null) {
                System.out.println(line);
            }

            while ((line = errorReader.readLine()) != null) {
                System.out.println(line);
            }

            proc.waitFor();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
