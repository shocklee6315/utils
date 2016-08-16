package com.shock.utils;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Created by shocklee on 16/8/16.
 */
public class AppTest extends TestSuite{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( CallBackTemplateUtilTest.class );
    }

    public static void main(String[] args) {
        TestRunner.run(AppTest.suite());
    }
}
