package com.shock.utils;

/**
 * Created by shocklee on 16/8/15.
 */
public abstract class CallBackTemplateUtil {


    /**
     * 有返回值的模板方法
     * @param k
     * @param callBack
     * @param <T>
     * @param <K>
     * @return
     */
    public static <T,K> T callTemplate( K k,CallBack<T,K> callBack ){
        return callBack.execute(k );
    }

    /**
     * 无返回值的模板方法
     * @param v
     * @param callback
     * @param <V>
     */
    public static <V > void callTemplateNoReturn(V v , CallBackNoReturn<V> callback){
        callback.execute(v);
    }

    public static void main(String[] args) {
        String a ="aaaa";
        System.out.println(callTemplate( a,new CallBack<Boolean, String>() {

            public Boolean execute(String s) {
                return s.contains("a");
            }
        } ));
        callTemplate( a, new CallBack<Void, String>() {

            public Void execute(String s) {
                System.out.println(s);
                return null;
            }
        } );
        callTemplateNoReturn(a, new CallBackNoReturn<String>() {
            public void execute(String s) {
                System.out.println(s);
            }
        });

    }

}
