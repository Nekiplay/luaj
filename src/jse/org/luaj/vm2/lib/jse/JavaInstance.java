/*******************************************************************************
* Copyright (c) 2011 Luaj.org. All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
******************************************************************************/
package org.luaj.vm2.lib.jse;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.LoaderClassPath;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;

/**
 * LuaValue that represents a Java instance.
 * <p>
 * Will respond to get() and set() by returning field values or methods. 
 * <p>
 * This class is not used directly.  
 * It is returned by calls to {@link CoerceJavaToLua#coerce(Object)} 
 * when a subclass of Object is supplied.
 * @see CoerceJavaToLua
 * @see CoerceLuaToJava
 */
public class JavaInstance extends LuaUserdata {

	JavaClass jclass;
	Map accessorCache;
	static final Object NOT_FOUND = new Object();

	private Class<?> dynamicBase;
	private static final Map<String, Class<?>> dynamicClasses = new ConcurrentHashMap<>();
	private static volatile int dynamicCounter = 0;

	public JavaInstance(Object instance) {
		super(instance);
	}

	public LuaValue get(LuaValue key) {
		if ( accessorCache != null ) {
			Object cached = accessorCache.get(key);
			if ( cached != null ) {
				if ( cached == NOT_FOUND )
					return super.get(key);
				if ( cached instanceof Field ) {
					try {
						return CoerceJavaToLua.coerce(((Field)cached).get(m_instance));
					} catch (Exception e) {
						throw new LuaError(e);
					}
				}
				return (LuaValue) cached;
			}
		}

		if ( jclass == null )
			jclass = JavaClass.forClass(m_instance.getClass());
		Field f = jclass.getField(key);
		if ( f != null ) {
			if ( accessorCache == null )
				accessorCache = new HashMap();
			accessorCache.put(key, f);
			try {
				return CoerceJavaToLua.coerce(f.get(m_instance));
			} catch (Exception e) {
				throw new LuaError(e);
			}
		}
		LuaValue m = jclass.getMethod(key);
		if ( m != null ) {
			if ( accessorCache == null )
				accessorCache = new HashMap();
			accessorCache.put(key, m);
			return m;
		}
		Class c = jclass.getInnerClass(key);
		if ( c != null ) {
			JavaClass jc = JavaClass.forClass(c);
			if ( accessorCache == null )
				accessorCache = new HashMap();
			accessorCache.put(key, jc);
			return jc;
		}
		if ( accessorCache == null )
			accessorCache = new HashMap();
		accessorCache.put(key, NOT_FOUND);
		return super.get(key);
	}

	public void set(LuaValue key, LuaValue value) {
		if ( jclass == null )
			jclass = JavaClass.forClass(m_instance.getClass());
		Field f = jclass.getField(key);
		if ( f != null )
			try {
				f.set(m_instance, CoerceLuaToJava.coerce(value, f.getType()));
				return;
			} catch (Exception e) {
				throw new LuaError(e);
			}
		setDynamicField(key, value);
	} 	

	private void setDynamicField(LuaValue key, LuaValue value) {
		String name = key.tojstring();
		if ( name == null || !name.matches("[A-Za-z_][A-Za-z0-9_]*") ) {
			super.set(key, value);
			return;
		}
		try {
			Class<?> base = dynamicBase != null ? dynamicBase : m_instance.getClass();
			ClassPool pool = ClassPool.getDefault();
			pool.insertClassPath(new LoaderClassPath(base.getClassLoader()));
			CtClass fieldType = fieldTypeFor(value, pool);
			Class<?> dyn = getOrCreateDynamicClass(base, name, fieldType);
			Object newInstance = allocateAndCopy(m_instance, dyn);
			m_instance = newInstance;
			dynamicBase = dyn;
			jclass = JavaClass.forClass(dyn);
			accessorCache = null;
			Field f = dyn.getField(name);
			f.set(newInstance, coerceTo(value, f.getType()));
		} catch (Exception e) {
			super.set(key, value);
		}
	}

	private static Class<?> getOrCreateDynamicClass(Class<?> base, String name, CtClass fieldType) throws Exception {
		String cacheKey = base.getName() + "#" + name;
		Class<?> existing = dynamicClasses.get(cacheKey);
		if ( existing != null )
			return existing;
		synchronized (dynamicClasses) {
			existing = dynamicClasses.get(cacheKey);
			if ( existing != null )
				return existing;
			ClassPool pool = ClassPool.getDefault();
			pool.insertClassPath(new LoaderClassPath(base.getClassLoader()));
			String subName = base.getName() + "$lua$dyn$" + (dynamicCounter++);
			CtClass sub = pool.makeClass(subName);
			sub.setSuperclass(pool.get(base.getName()));
			CtField cf = new CtField(fieldType, name, sub);
			cf.setModifiers(java.lang.reflect.Modifier.PUBLIC);
			sub.addField(cf);
			Class<?> c = sub.toClass(base.getClassLoader(), base.getProtectionDomain());
			dynamicClasses.put(cacheKey, c);
			return c;
		}
	}

	private static CtClass fieldTypeFor(LuaValue v, ClassPool pool) throws Exception {
		if ( v.isboolean() )
			return CtClass.booleanType;
		if ( v.isinttype() )
			return CtClass.intType;
		if ( v.isnumber() )
			return CtClass.doubleType;
		if ( v.isstring() )
			return pool.get("java.lang.String");
		return pool.get("java.lang.Object");
	}

	private static Object coerceTo(LuaValue v, Class<?> t) {
		if ( t == boolean.class || t == Boolean.class )
			return v.toboolean();
		if ( t == int.class || t == Integer.class || t == long.class || t == Long.class )
			return v.toint();
		if ( t == double.class || t == Double.class || t == float.class || t == Float.class )
			return v.todouble();
		if ( t == String.class )
			return v.tojstring();
		return CoerceLuaToJava.coerce(v, t);
	}

	private static Object allocateAndCopy(Object src, Class<?> dst) {
		Object o = allocateInstance(dst);
		Class<?> c = src.getClass();
		while ( c != null && c != Object.class ) {
			for ( Field f : c.getDeclaredFields() ) {
				if ( Modifier.isStatic(f.getModifiers()) )
					continue;
				f.setAccessible(true);
				try {
					Field df = dst.getDeclaredField(f.getName());
					df.setAccessible(true);
					df.set(o, f.get(src));
				} catch (NoSuchFieldException e) {
				} catch (Exception e) {
					throw new LuaError(e);
				}
			}
			c = c.getSuperclass();
		}
		return o;
	}

	private static Object allocateInstance(Class<?> c) {
		try {
			Class<?> uc = Class.forName("sun.misc.Unsafe");
			Field f = uc.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			Object u = f.get(null);
			return uc.getMethod("allocateInstance", Class.class).invoke(u, c);
		} catch (Exception e) {
			try {
				return c.getDeclaredConstructor().newInstance();
			} catch (Exception e2) {
				throw new LuaError(e2);
			}
		}
	}

}
