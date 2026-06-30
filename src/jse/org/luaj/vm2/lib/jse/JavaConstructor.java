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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * LuaValue that represents a particular public Java constructor.
 * <p>
 * May be called with arguments to return a JavaInstance 
 * created by calling the constructor.  
 * <p>
 * This class is not used directly.  
 * It is returned by calls to {@link JavaClass#new(LuaValue key)} 
 * when the value of key is "new".
 * @see CoerceJavaToLua
 * @see CoerceLuaToJava
 */
class JavaConstructor extends JavaMember {

	static final Map constructors = new ConcurrentHashMap();
	
	static JavaConstructor forConstructor(Constructor c) {
		JavaConstructor j = (JavaConstructor) constructors.get(c);
		if ( j == null )
			constructors.put( c, j = new JavaConstructor(c) );
		return j;
	}
	
	public static LuaValue forConstructors(JavaConstructor[] array) {
		return new Overload(array);
	}

	final Constructor constructor;
	
	private JavaConstructor(Constructor c) {
		super( c.getParameterTypes(), c.getModifiers() );
		this.constructor = c;
	}
	
	public Varargs invoke(Varargs args) {
		Object[] a = convertArgs(args);
		try {
			return CoerceJavaToLua.coerce( constructor.newInstance(a) );
		} catch (InvocationTargetException e) {
			throw new LuaError(e.getTargetException());
		} catch (Exception e) {
			return LuaValue.error("coercion error "+e);
		}
	}

	/**
	 * LuaValue that represents an overloaded Java constructor.
	 * <p>
	 * On invocation, will pick the best method from the list, and invoke it.
	 * <p>
	 * This class is not used directly.  
	 * It is returned by calls to calls to {@link JavaClass#get(LuaValue key)} 
	 * when key is "new" and there is more than one public constructor.
	 */
	static class Overload extends VarArgFunction {
		final JavaConstructor[] constructors;
		final Map cache;

		public Overload(JavaConstructor[] c) {
			this.constructors = c;
			this.cache = new HashMap();
		}

		public Varargs invoke(Varargs args) {
			long key = argsToKey(args);
			JavaConstructor best;
			synchronized (cache) {
				best = (JavaConstructor) cache.get(new Long(key));
			}
			if ( best == null ) {
				int score = CoerceLuaToJava.SCORE_UNCOERCIBLE;
				for ( int i = 0; i < constructors.length; i++ ) {
					int s = constructors[i].score(args);
					if ( s < score ) {
						score = s;
						best = constructors[i];
						if ( score == 0 )
							break;
					}
				}
				if ( best == null )
					LuaValue.error("no coercible public method");
				synchronized (cache) {
					cache.put(new Long(key), best);
				}
			}
			return best.invoke(args);
		}

		static long argsToKey(Varargs args) {
			int n = args.narg();
			long key = n;
			for ( int i = 1; i <= n; i++ ) {
				LuaValue v = args.arg(i);
				int type = v.type();
				key = (key << 6) ^ type;
				if ( type == LuaValue.TNUMBER ) {
					key = (key << 2) ^ (v.isint() ? 0x1 : 0x2);
				} else if ( type == LuaValue.TUSERDATA ) {
					Object obj = v.touserdata();
					key = (key << 6) ^ (obj != null ? obj.getClass().hashCode() : 0);
				}
			}
			return key;
		}
	}
}
