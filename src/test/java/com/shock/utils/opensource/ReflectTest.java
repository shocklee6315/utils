/**
 * Copyright (c) 2011-2016, Data Geekery GmbH (http://www.datageekery.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shock.utils.opensource;

import static com.shock.utils.opensource.Reflect.*;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.HashMap;
import java.util.Map;

import com.shock.utils.exception.ReflectionException;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Lukas Eder
 */
public class ReflectTest {

    @Test
    public void testOn() {
        assertEquals(on(Object.class), on("java.lang.Object", ClassLoader.getSystemClassLoader()));
        assertEquals(on(Object.class), on("java.lang.Object"));
        assertEquals(on(Object.class).get(), on("java.lang.Object").get());
        assertEquals(Object.class, on(Object.class).get());
        assertEquals("abc", on((Object) "abc").get());
        assertEquals(1, on(1).get());

        try {
            on("asdf");
            fail();
        }
        catch (ReflectionException expected) {}

        try {
            on("asdf", ClassLoader.getSystemClassLoader());
            fail();
        }
        catch (ReflectionException expected) {}
    }

    @Test
    public void testConstructors() {
        assertEquals("", on(String.class).create().get());
        assertEquals("abc", on(String.class).create("abc").get());
        assertEquals("abc", on(String.class).create("abc".getBytes()).get());
        assertEquals("abc", on(String.class).create("abc".toCharArray()).get());
        assertEquals("b", on(String.class).create("abc".toCharArray(), 1, 1).get());

        try {
            on(String.class).create(new Object());
            fail();
        }
        catch (ReflectionException expected) {}
    }

    @Test
    public void testPrivateConstructor() {
        assertNull(on(PrivateConstructors.class).create().get("string"));
        assertEquals("abc", on(PrivateConstructors.class).create("abc").get("string"));
    }

    @Test
    public void testConstructorsWithAmbiguity() {
        // [#5] Re-enact when this is implemented
        assumeTrue(false);

        Test2 test;

        test = on(Test2.class).create().get();
        assertEquals(null, test.n);
        assertEquals(Test2.ConstructorType.NO_ARGS, test.constructorType);

        test = on(Test2.class).create("abc").get();
        assertEquals("abc", test.n);
        assertEquals(Test2.ConstructorType.OBJECT, test.constructorType);

        test = on(Test2.class).create(new Long("1")).get();
        assertEquals(1L, test.n);
        assertEquals(Test2.ConstructorType.NUMBER, test.constructorType);

        test = on(Test2.class).create(1).get();
        assertEquals(1, test.n);
        assertEquals(Test2.ConstructorType.INTEGER, test.constructorType);

        test = on(Test2.class).create('a').get();
        assertEquals('a', test.n);
        assertEquals(Test2.ConstructorType.OBJECT, test.constructorType);
    }

    @Test
    public void testMethods() {
        // Instance methods
        // ----------------
        assertEquals("", on((Object) " ").call("trim").get());
        assertEquals("12", on((Object) " 12 ").call("trim").get());
        assertEquals("34", on((Object) "1234").call("substring", 2).get());
        assertEquals("12", on((Object) "1234").call("substring", 0, 2).get());
        assertEquals("1234", on((Object) "12").call("concat", "34").get());
        assertEquals("123456", on((Object) "12").call("concat", "34").call("concat", "56").get());
        assertEquals(2, on((Object) "1234").call("indexOf", "3").get());
        assertEquals(2.0f, on((Object) "1234").call("indexOf", "3").call("floatValue").get());
        assertEquals("2", on((Object) "1234").call("indexOf", "3").call("toString").get());

        // Static methods
        // --------------
        assertEquals("true", on(String.class).call("valueOf", true).get());
        assertEquals("1", on(String.class).call("valueOf", 1).get());
        assertEquals("abc", on(String.class).call("valueOf", "abc".toCharArray()).get());
        assertEquals("abc", on(String.class).call("copyValueOf", "abc".toCharArray()).get());
        assertEquals("b", on(String.class).call("copyValueOf", "abc".toCharArray(), 1, 1).get());
    }

    @Test
    public void testVoidMethods() {
        // Instance methods
        // ----------------
        Test4 test4 = new Test4();
        assertEquals(test4, on(test4).call("i_method").get());

        // Static methods
        // --------------
        assertEquals(Test4.class, on(Test4.class).call("s_method").get());
    }

    @Test
    public void testPrivateMethods() throws Exception {
        // Instance methods
        // ----------------
        Test8 test8 = new Test8();
        assertEquals(test8, on(test8).call("i_method").get());

        // Static methods
        // --------------
        assertEquals(Test8.class, on(Test8.class).call("s_method").get());

    }

    @Test
    public void testNullArguments() throws Exception {
        Test9 test9 = new Test9();
        on(test9).call("put", "key", "value");
        assertTrue(test9.map.containsKey("key"));
        assertEquals("value", test9.map.get("key"));

        on(test9).call("put", "key", null);
        assertTrue(test9.map.containsKey("key"));
        assertNull(test9.map.get("key"));
    }

    @Test
    public void testPublicMethodsAreFoundInHierarchy() throws Exception {
        TestHierarchicalMethodsSubclass subclass = new TestHierarchicalMethodsSubclass();
        assertEquals(TestHierarchicalMethodsBase.PUBLIC_RESULT, on(subclass).call("pub_base_method", 1).get());
    }

    @Test
    public void testPrivateMethodsAreFoundInHierarchy() throws Exception {
        TestHierarchicalMethodsSubclass subclass = new TestHierarchicalMethodsSubclass();
        on(subclass).call("very_priv_method").get();
    }

    @Test
    public void testPrivateMethodsAreFoundOnDeclaringClass() throws Exception {
        TestHierarchicalMethodsSubclass subclass = new TestHierarchicalMethodsSubclass();
        assertEquals(TestHierarchicalMethodsSubclass.PRIVATE_RESULT, on(subclass).call("priv_method", 1).get());

        TestHierarchicalMethodsBase baseClass = new TestHierarchicalMethodsBase();
        assertEquals(TestHierarchicalMethodsBase.PRIVATE_RESULT, on(baseClass).call("priv_method", 1).get());
    }

    @Test
    public void testMethodsWithAmbiguity() {
        // [#5] Re-enact when this is implemented
        assumeTrue(false);

        Test3 test;

        test = on(Test3.class).create().call("method").get();
        assertEquals(null, test.n);
        assertEquals(Test3.MethodType.NO_ARGS, test.methodType);

        test = on(Test3.class).create().call("method", "abc").get();
        assertEquals("abc", test.n);
        assertEquals(Test3.MethodType.OBJECT, test.methodType);

        test = on(Test3.class).create().call("method", new Long("1")).get();
        assertEquals(1L, test.n);
        assertEquals(Test3.MethodType.NUMBER, test.methodType);

        test = on(Test3.class).create().call("method", 1).get();
        assertEquals(1, test.n);
        assertEquals(Test3.MethodType.INTEGER, test.methodType);

        test = on(Test3.class).create().call("method", 'a').get();
        assertEquals('a', test.n);
        assertEquals(Test3.MethodType.OBJECT, test.methodType);
    }

    @Test
    public void testFields() throws Exception {
        // Instance methods
        // ----------------
        Test1 test1 = new Test1();
        assertEquals(1, on(test1).set("I_INT1", 1).get("I_INT1"));
        assertEquals(1, on(test1).field("I_INT1").get());
        assertEquals(1, on(test1).set("I_INT2", 1).get("I_INT2"));
        assertEquals(1, on(test1).field("I_INT2").get());
        assertNull(on(test1).set("I_INT2", null).get("I_INT2"));
        assertNull(on(test1).field("I_INT2").get());

        // Static methods
        // --------------
        assertEquals(1, on(Test1.class).set("S_INT1", 1).get("S_INT1"));
        assertEquals(1, on(Test1.class).field("S_INT1").get());
        assertEquals(1, on(Test1.class).set("S_INT2", 1).get("S_INT2"));
        assertEquals(1, on(Test1.class).field("S_INT2").get());
        assertNull(on(Test1.class).set("S_INT2", null).get("S_INT2"));
        assertNull(on(Test1.class).field("S_INT2").get());

        // Hierarchies
        // -----------
        TestHierarchicalMethodsSubclass test2 = new TestHierarchicalMethodsSubclass();
        assertEquals(1, on(test2).set("invisibleField1", 1).get("invisibleField1"));
        assertEquals(1, accessible(TestHierarchicalMethodsBase.class.getDeclaredField("invisibleField1")).get(test2));

        assertEquals(1, on(test2).set("invisibleField2", 1).get("invisibleField2"));
        assertEquals(0, accessible(TestHierarchicalMethodsBase.class.getDeclaredField("invisibleField2")).get(test2));
        assertEquals(1, accessible(TestHierarchicalMethodsSubclass.class.getDeclaredField("invisibleField2")).get(test2));

        assertEquals(1, on(test2).set("invisibleField3", 1).get("invisibleField3"));
        assertEquals(1, accessible(TestHierarchicalMethodsSubclass.class.getDeclaredField("invisibleField3")).get(test2));

        assertEquals(1, on(test2).set("visibleField1", 1).get("visibleField1"));
        assertEquals(1, accessible(TestHierarchicalMethodsBase.class.getDeclaredField("visibleField1")).get(test2));

        assertEquals(1, on(test2).set("visibleField2", 1).get("visibleField2"));
        assertEquals(0, accessible(TestHierarchicalMethodsBase.class.getDeclaredField("visibleField2")).get(test2));
        assertEquals(1, accessible(TestHierarchicalMethodsSubclass.class.getDeclaredField("visibleField2")).get(test2));

        assertEquals(1, on(test2).set("visibleField3", 1).get("visibleField3"));
        assertEquals(1, accessible(TestHierarchicalMethodsSubclass.class.getDeclaredField("visibleField3")).get(test2));

        assertNull(accessible(null));
    }

    @Test
    public void testFieldMap() {
        // Instance methods
        // ----------------
        Test1 test1 = new Test1();
        assertEquals(3, on(test1).fields().size());
        assertTrue(on(test1).fields().containsKey("I_INT1"));
        assertTrue(on(test1).fields().containsKey("I_INT2"));
        assertTrue(on(test1).fields().containsKey("I_DATA"));

        assertEquals(1, on(test1).set("I_INT1", 1).fields().get("I_INT1").get());
        assertEquals(1, on(test1).fields().get("I_INT1").get());
        assertEquals(1, on(test1).set("I_INT2", 1).fields().get("I_INT2").get());
        assertEquals(1, on(test1).fields().get("I_INT2").get());
        assertNull(on(test1).set("I_INT2", null).fields().get("I_INT2").get());
        assertNull(on(test1).fields().get("I_INT2").get());

        // Static methods
        // --------------
        assertEquals(3, on(Test1.class).fields().size());
        assertTrue(on(Test1.class).fields().containsKey("S_INT1"));
        assertTrue(on(Test1.class).fields().containsKey("S_INT2"));
        assertTrue(on(Test1.class).fields().containsKey("S_DATA"));

        assertEquals(1, on(Test1.class).set("S_INT1", 1).fields().get("S_INT1").get());
        assertEquals(1, on(Test1.class).fields().get("S_INT1").get());
        assertEquals(1, on(Test1.class).set("S_INT2", 1).fields().get("S_INT2").get());
        assertEquals(1, on(Test1.class).fields().get("S_INT2").get());
        assertNull(on(Test1.class).set("S_INT2", null).fields().get("S_INT2").get());
        assertNull(on(Test1.class).fields().get("S_INT2").get());

        // Hierarchies
        // -----------
        TestHierarchicalMethodsSubclass test2 = new TestHierarchicalMethodsSubclass();
        assertEquals(6, on(test2).fields().size());
        assertTrue(on(test2).fields().containsKey("invisibleField1"));
        assertTrue(on(test2).fields().containsKey("invisibleField2"));
        assertTrue(on(test2).fields().containsKey("invisibleField3"));
        assertTrue(on(test2).fields().containsKey("visibleField1"));
        assertTrue(on(test2).fields().containsKey("visibleField2"));
        assertTrue(on(test2).fields().containsKey("visibleField3"));
    }

    @Test
    public void testFieldAdvanced() {
        on(Test1.class).set("S_DATA", on(Test1.class).create())
                      .field("S_DATA")
                      .set("I_DATA", on(Test1.class).create())
                      .field("I_DATA")
                      .set("I_INT1", 1)
                      .set("S_INT1", 2);
        assertEquals(2, Test1.S_INT1);
        assertEquals(null, Test1.S_INT2);
        assertEquals(0, Test1.S_DATA.I_INT1);
        assertEquals(null, Test1.S_DATA.I_INT2);
        assertEquals(1, Test1.S_DATA.I_DATA.I_INT1);
        assertEquals(null, Test1.S_DATA.I_DATA.I_INT2);
    }

    @Test
    public void testProxy() {
        assertEquals("abc", on((Object) "abc").as(Test5.class).substring(0));
        assertEquals("bc", on((Object) "abc").as(Test5.class).substring(1));
        assertEquals("c", on((Object) "abc").as(Test5.class).substring(2));

        assertEquals("a", on((Object) "abc").as(Test5.class).substring(0, 1));
        assertEquals("b", on((Object) "abc").as(Test5.class).substring(1, 2));
        assertEquals("c", on((Object) "abc").as(Test5.class).substring(2, 3));

        assertEquals("abc", on((Object) "abc").as(Test5.class).substring(new Integer(0)));
        assertEquals("bc", on((Object) "abc").as(Test5.class).substring(new Integer(1)));
        assertEquals("c", on((Object) "abc").as(Test5.class).substring(new Integer(2)));

        assertEquals("a", on((Object) "abc").as(Test5.class).substring(new Integer(0), new Integer(1)));
        assertEquals("b", on((Object) "abc").as(Test5.class).substring(new Integer(1), new Integer(2)));
        assertEquals("c", on((Object) "abc").as(Test5.class).substring(new Integer(2), new Integer(3)));
    }

    @Test
    public void testMapProxy() {

        @SuppressWarnings({ "unused", "serial" })
        class MyMap extends HashMap<String, Object> {
            String baz;
            public void setBaz(String baz) {
                this.baz = "MyMap: " + baz;
            }

            public String getBaz() {
                return baz;
            }
        }
        Map<String, Object> map = new MyMap();

        on(map).as(Test6.class).setFoo("abc");
        assertEquals(1, map.size());
        assertEquals("abc", map.get("foo"));
        assertEquals("abc", on(map).as(Test6.class).getFoo());

        on(map).as(Test6.class).setBar(true);
        assertEquals(2, map.size());
        assertEquals(true, map.get("bar"));
        assertEquals(true, on(map).as(Test6.class).isBar());

        on(map).as(Test6.class).setBaz("baz");
        assertEquals(2, map.size());
        assertEquals(null, map.get("baz"));
        assertEquals("MyMap: baz", on(map).as(Test6.class).getBaz());

        try {
            on(map).as(Test6.class).testIgnore();
            fail();
        }
        catch (ReflectionException expected) {}
    }

    @Test
    public void testPrivateField() throws Exception {
        class Foo {
            private String bar;
        }

        Foo foo = new Foo();
        on(foo).set("bar", "FooBar");
        assertThat(foo.bar, is("FooBar"));
        assertEquals("FooBar", on(foo).get("bar"));

        on(foo).set("bar", null);
        assertNull(foo.bar);
        assertNull(on(foo).get("bar"));
    }

    @Test
    public void testType() throws Exception {
        assertEquals(Object.class, on(new Object()).type());
        assertEquals(Object.class, on(Object.class).type());
        assertEquals(Integer.class, on(1).type());
        assertEquals(Integer.class, on(Integer.class).type());
    }

    @Test
    public void testCreateWithNulls() throws Exception {
        Test2 test2 = on(Test2.class).create((Object) null).<Test2>get();
        assertNull(test2.n);
        // Can we make any assertions about the actual construct being called?
        // assertEquals(Test2.ConstructorType.OBJECT, test2.constructorType);
    }

    @Test
    public void testCreateWithPrivateConstructor() throws Exception {
        Test10 t1 = on(Test10.class).create(1).get();
        assertEquals(1, (int) t1.i);
        assertNull(t1.s);

        Test10 t2 = on(Test10.class).create("a").get();
        assertNull(t2.i);
        assertEquals("a", t2.s);

        Test10 t3 = on(Test10.class).create("a", 1).get();
        assertEquals(1, (int) t3.i);
        assertEquals("a", t3.s);
    }

    @Test
    public void testHashCode() {
        Object object = new Object();
        assertEquals(on(object).hashCode(), object.hashCode());
    }

    @Test
    public void testToString() {
        Object object = new Object() {
            @Override
            public String toString() {
                return "test";
            }
        };
        assertEquals(on(object).toString(), object.toString());
    }

    @Test
    public void testEquals() {
        Object object = new Object();
        Reflect a = on(object);
        Reflect b = on(object);
        Reflect c = on(object);

        assertTrue(b.equals(a));
        assertTrue(a.equals(b));
        assertTrue(b.equals(c));
        assertTrue(a.equals(c));
        //noinspection ObjectEqualsNull
        assertFalse(a.equals(null));
    }

    @Before
    public void setUp() {
        Test1.S_INT1 = 0;
        Test1.S_INT2 = null;
        Test1.S_DATA = null;
    }
}