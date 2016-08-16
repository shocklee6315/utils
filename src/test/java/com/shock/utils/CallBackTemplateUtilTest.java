package com.shock.utils;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shocklee on 16/8/16.
 */
public class CallBackTemplateUtilTest {

    private static transient Logger logger = LoggerFactory.getLogger(CallBackTemplateUtilTest.class);
    @Test
    public void testNoReturn(){

        Resource resource = Resource.getInstance();
        CallBackTemplateUtil.callTemplateNoReturn(resource, new CallBackNoReturn<Resource>() {
            @Override
            public void execute(Resource resource) {
                resource.workNoReturn();
            }
        });
        resource.close();
    }

    @Test
    public void testWork(){
        Resource resource = Resource.getInstance();
        CallBackTemplateUtil.callTemplate(resource, new CallBack<String, Resource>() {
            @Override
            public String execute(Resource resource) {
                return resource.work();
            }
        });
        resource.close();
    }
    @Test
    public void testCustomerTemplate(){

        Assert.assertEquals("SUCESS" ,callTemplate(new CallBack<String, Resource>() {
            @Override
            public String execute(Resource resource) {
                return resource.work();
            }
        }));
    }

    public static<T> T callTemplate(CallBack<T,Resource> callback){
        Resource resource =Resource.getInstance();
        try{
            return callback.execute(resource);
        }finally {
            resource.close();
        }

    }

    public static class Resource{
        public void close(){
            logger.error("close");
        }

        public static Resource getInstance(){
            return new Resource();
        }
        public void workNoReturn(){
            logger.error("no return ");
        }

        public String work(){
            logger.error("work");
            return "SUCESS";
        }
    }


}
