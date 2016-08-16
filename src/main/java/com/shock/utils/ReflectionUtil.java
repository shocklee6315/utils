package com.shock.utils;


import com.shock.utils.exception.ConstructionException;
import com.shock.utils.opensource.ConcurrentReferenceHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class ReflectionUtil {

	private static final Logger logger = LoggerFactory.getLogger(ReflectionUtil.class);

	/**
	 * cache declaredMethods use softrefrence
	 */
	private static final Map<Class<?>, Method[]> declaredMethodsCache =
			new ConcurrentReferenceHashMap<Class<?>, Method[]>(256);


	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		}
		catch (Throwable ex) {
			// Cannot access thread context ClassLoader - falling back to system class loader...
		}
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = ReflectionUtil.class.getClassLoader();
		}
		return cl;
	}



	public static Class forName(String className)throws ClassNotFoundException{
		return getDefaultClassLoader().loadClass(className);
	}

	public static <T> T instantiateClass(Constructor<T> ctor, Object... args) throws ConstructionException {
		Assert.notNull(ctor, "Constructor must not be null");
		try {
			makeAccessible(ctor);
			return ctor.newInstance(args);
		}
		catch (InstantiationException ex) {
			throw new ConstructionException(ctor.getDeclaringClass(),
					"Is it an abstract class?", ex);
		}
		catch (IllegalAccessException ex) {
			throw new ConstructionException(ctor.getDeclaringClass(),
					"Is the constructor accessible?", ex);
		}
		catch (IllegalArgumentException ex) {
			throw new ConstructionException(ctor.getDeclaringClass(),
					"Illegal arguments for constructor", ex);
		}
		catch (InvocationTargetException ex) {
			throw new ConstructionException(ctor.getDeclaringClass(),
					"Constructor threw exception", ex.getTargetException());
		}
	}

	public static void makeAccessible(Constructor<?> ctor) {
		if ((!Modifier.isPublic(ctor.getModifiers()) ||
				!Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) && !ctor.isAccessible()) {
			ctor.setAccessible(true);
		}
	}


	public static Field getField(Class<?> cls, String fieldName) {
		while (cls != null && cls.equals(Object.class) == false) {
			Field field = null;
			try {
				field = cls.getDeclaredField(fieldName);
			} catch (SecurityException e) {
				logger.error("Unable to get field " + fieldName
						+ " from class " + cls.getName(), e);
			} catch (NoSuchFieldException e) {
				logger.debug("Unable to get field " + fieldName
						+ " from class " + cls.getName() + ". "
						+ "If there is any super class for class "
						+ cls.getName()
						+ ", it will be tried for getting field ...");
			}
			if (field != null) {
				field.setAccessible(true);
				return field;
			}
			cls = cls.getSuperclass();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static List<Field> getAllFields(Class<?> cls) {
		List<Field> fields = new ArrayList<Field>();
		createFields(cls, fields);
		return fields;
	}

	@SuppressWarnings("unchecked")
	public static List<Field> getAllFields(Class<?> cls,
										   Class<? extends Annotation>... annotationFilters) {
		List<Field> fields = new ArrayList<Field>();
		createFields(cls, fields, annotationFilters);
		return fields;
	}

	@SuppressWarnings("unchecked")
	private static void createFields(Class<?> cls, List<Field> fields,
									 Class<? extends Annotation>... annotationFilters) {
		if (cls == null || cls.equals(Object.class)) {
			return;
		}

		Class<?> superCls = cls.getSuperclass();
		createFields(superCls, fields, annotationFilters);

		for (Field f : cls.getDeclaredFields()) {
			f.setAccessible(true);
			if (annotationFilters == null || annotationFilters.length == 0) {
				fields.add(f);
			} else {
				for (Class<? extends Annotation> annotationFilter : annotationFilters) {
					if (f.getAnnotation(annotationFilter) != null) {
						fields.add(f);
						break;
					}
				}
			}
		}
	}

	public static Method getMethod(Class<?> cls, String methodName) {
		while (cls != null && cls.equals(Object.class) == false) {
			Method method = null;
			for (Method m : cls.getDeclaredMethods()) {
				if (m.getName().equals(methodName)) {
					method = m;
					break;
				}
			}
			if (method == null) {
				logger.warn("Unable to get method " + methodName
						+ " from class " + cls.getName() + ". "
						+ "If there is any super class for class "
						+ cls.getName()
						+ ", it will be tried for getting method ...");
			} else {
				method.setAccessible(true);
				return method;
			}
			cls = cls.getSuperclass();
		}
		return null;
	}


	public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(name, "Method name must not be null");
		Class<?> searchType = clazz;
		while (searchType != null) {
			Method[] methods = (searchType.isInterface() ? searchType.getMethods() : getDeclaredMethods(searchType));
			for (Method method : methods) {
				if (name.equals(method.getName()) &&
						(paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
					return method;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}

	public static Method findMethod(Class<?> clazz, String name) {
		return findMethod(clazz, name, new Class<?>[0]);
	}


	@SuppressWarnings("unchecked")
	public static List<Method> getAllMethods(Class<?> cls) {
		List<Method> methods = new ArrayList<Method>();
		createMethods(cls, methods);
		return methods;
	}

	@SuppressWarnings("unchecked")
	public static List<Method> getAllMethods(Class<?> cls,
											 Class<? extends Annotation>... annotationFilters) {
		List<Method> methods = new ArrayList<Method>();
		createMethods(cls, methods, annotationFilters);
		return methods;
	}

	@SuppressWarnings("unchecked")
	private static void createMethods(Class<?> cls, List<Method> methods,
									  Class<? extends Annotation>... annotationFilters) {
		if (cls == null || cls.equals(Object.class)) {
			return;
		}

		Class<?> superCls = cls.getSuperclass();
		createMethods(superCls, methods, annotationFilters);

		for (Method m : cls.getDeclaredMethods()) {
			m.setAccessible(true);
			if (annotationFilters == null || annotationFilters.length == 0) {
				methods.add(m);
			} else {
				for (Class<? extends Annotation> annotationFilter : annotationFilters) {
					if (m.getAnnotation(annotationFilter) != null) {
						methods.add(m);
						break;
					}
				}
			}
		}
	}

	public static boolean isPrimitiveType(String clsName) {
		if (clsName.equalsIgnoreCase("boolean")) {
			return true;
		} else if (clsName.equalsIgnoreCase("byte")) {
			return true;
		} else if (clsName.equalsIgnoreCase("char")) {
			return true;
		} else if (clsName.equalsIgnoreCase("short")) {
			return true;
		} else if (clsName.equalsIgnoreCase("int")) {
			return true;
		} else if (clsName.equalsIgnoreCase("float")) {
			return true;
		} else if (clsName.equalsIgnoreCase("long")) {
			return true;
		} else if (clsName.equalsIgnoreCase("double")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isPrimitiveType(Class<?> cls) {
		if (cls.equals(boolean.class) || cls.equals(Boolean.class)) {
			return true;
		} else if (cls.equals(byte.class) || cls.equals(Byte.class)) {
			return true;
		} else if (cls.equals(char.class) || cls.equals(Character.class)) {
			return true;
		} else if (cls.equals(short.class) || cls.equals(Short.class)) {
			return true;
		} else if (cls.equals(int.class) || cls.equals(Integer.class)) {
			return true;
		} else if (cls.equals(float.class) || cls.equals(Float.class)) {
			return true;
		} else if (cls.equals(long.class) || cls.equals(Long.class)) {
			return true;
		} else if (cls.equals(double.class) || cls.equals(Double.class)) {
			return true;
		} else if (cls.equals(String.class)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isNonPrimitiveType(String clsName) {
		return !isPrimitiveType(clsName);
	}

	public static boolean isNonPrimitiveType(Class<?> cls) {
		return !isPrimitiveType(cls);
	}

	public static boolean isComplexType(Class<?> cls) {
		if (isPrimitiveType(cls)) {
			return false;
		} else if (cls.isEnum()) {
			return false;
		} else if (cls.equals(String.class)) {
			return false;
		} else if (isCollectionType(cls)) {
			return false;
		} else if (List.class.isAssignableFrom(cls)) {
			return false;
		} else {
			return true;
		}
	}

	public static boolean isCollectionType(Class<?> cls) {
		if (cls.isArray()) {
			return true;
		} else if (List.class.isAssignableFrom(cls)) {
			return true;
		} else if (Set.class.isAssignableFrom(cls)) {
			return true;
		} else if (Map.class.isAssignableFrom(cls)) {
			return true;
		} else {
			return false;
		}
	}

	public static Class<?> getNonPrimitiveType(String clsName) {
		if (clsName.equalsIgnoreCase("boolean")) {
			return Boolean.class;
		} else if (clsName.equalsIgnoreCase("byte")) {
			return Byte.class;
		} else if (clsName.equalsIgnoreCase("char")) {
			return Character.class;
		} else if (clsName.equalsIgnoreCase("short")) {
			return Short.class;
		} else if (clsName.equalsIgnoreCase("int")) {
			return Integer.class;
		} else if (clsName.equalsIgnoreCase("float")) {
			return Float.class;
		} else if (clsName.equalsIgnoreCase("long")) {
			return Long.class;
		} else if (clsName.equalsIgnoreCase("double")) {
			return Double.class;
		} else {
			try {
				return Class.forName(clsName);
			} catch (ClassNotFoundException e) {
				logger.error("Unable to get class " + clsName, e);
				return null;
			}
		}
	}

	public static Class<?> getNonPrimitiveType(Class<?> cls) {
		if (cls.equals(boolean.class)) {
			return Boolean.class;
		} else if (cls.equals(byte.class)) {
			return Byte.class;
		} else if (cls.equals(char.class)) {
			return Character.class;
		} else if (cls.equals(short.class)) {
			return Short.class;
		} else if (cls.equals(int.class)) {
			return Integer.class;
		} else if (cls.equals(float.class)) {
			return Float.class;
		} else if (cls.equals(long.class)) {
			return Long.class;
		} else if (cls.equals(double.class)) {
			return Double.class;
		} else {
			return cls;
		}
	}

	public static boolean isDecimalType(Class<?> cls) {

		if (cls.equals(byte.class) || cls.equals(Byte.class)) {
			return true;
		} else if (cls.equals(short.class) || cls.equals(Short.class)) {
			return true;
		} else if (cls.equals(int.class) || cls.equals(Integer.class)) {
			return true;
		} else if (cls.equals(long.class) || cls.equals(Long.class)) {
			return true;
		} else {
			return false;
		}
	}


	public static Object invokeMethod(Method method, Object target) {
		return invokeMethod(method, target, new Object[0]);
	}

	public static Object invokeMethod(Method method, Object target, Object... args) {
		try {
			return method.invoke(target, args);
		}
		catch (Exception ex) {
			handleReflectionException(ex);
		}
		throw new IllegalStateException("Should never get here");
	}

	public static void handleReflectionException(Exception ex) {
		if (ex instanceof NoSuchMethodException) {
			throw new IllegalStateException("Method not found: " + ex.getMessage());
		}
		if (ex instanceof IllegalAccessException) {
			throw new IllegalStateException("Could not access method: " + ex.getMessage());
		}
		if (ex instanceof InvocationTargetException) {
			handleInvocationTargetException((InvocationTargetException) ex);
		}
		if (ex instanceof RuntimeException) {
			throw (RuntimeException) ex;
		}
		throw new UndeclaredThrowableException(ex);
	}

	public static void rethrowRuntimeException(Throwable ex) {
		if (ex instanceof RuntimeException) {
			throw (RuntimeException) ex;
		}
		if (ex instanceof Error) {
			throw (Error) ex;
		}
		throw new UndeclaredThrowableException(ex);
	}
	public static void handleInvocationTargetException(InvocationTargetException ex) {
		rethrowRuntimeException(ex.getTargetException());
	}

	private static Method[] getDeclaredMethods(Class<?> clazz) {
		Method[] result = declaredMethodsCache.get(clazz);
		if (result == null) {
			result = clazz.getDeclaredMethods();
			declaredMethodsCache.put(clazz, result);
		}
		return result;
	}
}
