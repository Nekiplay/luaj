/*******************************************************************************
* Copyright (c) 2009 Luaj.org. All rights reserved.
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
*******************************************************************************/
package org.luaj.vm2;

import org.luaj.vm2.lib.MathLib;

public class LuaLong extends LuaNumber {

	private static final LuaLong[] longValues = new LuaLong[512];
	static {
		for ( int i=0; i<512; i++ )
			longValues[i] = new LuaLong(i-256);
	}

	public static LuaLong valueOf(long l) {
		int i = (int) l;
		return l==i && i>=-256 && i<=255? longValues[i+256]: new LuaLong(l);
	};
	
	public static LuaNumber valueOf(long l, boolean forceLong) {
		if (forceLong) return valueOf(l);
		int i = (int) l;
		return l==i? (i<=255 && i>=-256? longValues[i+256]:
			(LuaNumber) LuaInteger.valueOf(i)):
			(LuaNumber) LuaDouble.valueOf(l);
	}
	
	public final long v;
	
	LuaLong(long l) {
		this.v = l;
	}
	
	public boolean isint() {		return true;	}
	public boolean isinttype() {	return false;	}
	public boolean islong() {		return true;	}
	
	public byte    tobyte()        { return (byte) v; }
	public char    tochar()        { return (char) v; }
	public double  todouble()      { return v; }
	public float   tofloat()       { return v; }
	public int     toint()         { return (int) v; }
	public long    tolong()        { return v; }
	public short   toshort()       { return (short) v; }

	public double      optdouble(double defval)            { return v; }
	public int         optint(int defval)                  { return (int) v;  }
	public LuaInteger  optinteger(LuaInteger defval)       { return LuaInteger.valueOf((int)v); }
	public long        optlong(long defval)                { return v; }

	public String tojstring() {
		return Long.toString(v);
	}

	public LuaString strvalue() {
		return LuaString.valueOf(Long.toString(v));
	}
		
	public LuaString optstring(LuaString defval) {
		return LuaString.valueOf(Long.toString(v));
	}
	
	public LuaValue tostring() {
		return LuaString.valueOf(Long.toString(v));
	}
		
	public String optjstring(String defval) {
		return Long.toString(v);
	}
	
	public LuaInteger checkinteger() {
		return LuaInteger.valueOf((int)v);
	}
	
	public boolean isstring() {
		return true;
	}
	
	public int hashCode() {
		return (int) (v ^ (v >>> 32));
	}

	public static int hashCode(long x) {
		return (int) (x ^ (x >>> 32));
	}

	public LuaValue neg() { return valueOf(-v); }
	
	public boolean equals(Object o) { return o instanceof LuaLong? ((LuaLong)o).v == v: false; }
	
	public LuaValue eq( LuaValue val )    { return val.raweq(v)? TRUE: FALSE; }
	public boolean eq_b( LuaValue val )   { return val.raweq(v); }
	
	public boolean raweq( LuaValue val )  { return val.raweq(v); }
	public boolean raweq( double val )    { return v == val; }
	public boolean raweq( int val )       { return v == val; }
	public boolean raweq( long val )       { return v == val; }
	
	public LuaValue   add( LuaValue rhs )        { return rhs.add(v); }
	public LuaValue   add( double lhs )     { return LuaDouble.valueOf(lhs + v); }
	public LuaValue   add( int lhs )        { return valueOf(lhs + v); }
	public LuaValue   add( long lhs )        { return valueOf(lhs + v); }
	public LuaValue   sub( LuaValue rhs )        { return rhs.subFrom(v); }
	public LuaValue   sub( double rhs )        { return LuaDouble.valueOf(v - rhs); }
	public LuaValue   sub( int rhs )        { return valueOf(v - rhs); }
	public LuaValue   sub( long rhs )        { return valueOf(v - rhs); }
	public LuaValue   subFrom( double lhs )   { return LuaDouble.valueOf(lhs - v); }
	public LuaValue   subFrom( int lhs )      { return valueOf(lhs - v); }
	public LuaValue   subFrom( long lhs )    { return valueOf(lhs - v); }
	public LuaValue   mul( LuaValue rhs )        { return rhs.mul(v); }
	public LuaValue   mul( double lhs )   { return LuaDouble.valueOf(lhs * v); }
	public LuaValue   mul( int lhs )      { return valueOf(lhs * v); }
	public LuaValue   mul( long lhs )      { return valueOf(lhs * v); }
	public LuaValue   pow( LuaValue rhs )        { return rhs.powWith(v); }
	public LuaValue   pow( double rhs )        { return MathLib.dpow(v,rhs); }
	public LuaValue   pow( int rhs )        { return MathLib.dpow(v,rhs); }
	public LuaValue   pow( long rhs )        { return MathLib.dpow(v,rhs); }
	public LuaValue   powWith( double lhs )   { return MathLib.dpow(lhs,v); }
	public LuaValue   powWith( int lhs )      { return MathLib.dpow(lhs,v); }
	public LuaValue   powWith( long lhs )     { return MathLib.dpow(lhs,v); }
	public LuaValue   div( LuaValue rhs )        { return rhs.divInto(v); }
	public LuaValue   div( double rhs )        { return LuaDouble.ddiv(v,rhs); }
	public LuaValue   div( int rhs )        { return LuaDouble.ddiv(v,rhs); }
	public LuaValue   div( long rhs )        { return LuaDouble.ddiv(v,rhs); }
	public LuaValue   divInto( double lhs )   { return LuaDouble.ddiv(lhs,v); }
	public LuaValue   divInto( int lhs )      { return LuaDouble.ddiv(lhs,v); }
	public LuaValue   divInto( long lhs )     { return LuaDouble.ddiv(lhs,v); }
	public LuaValue   mod( LuaValue rhs )        { return rhs.modFrom(v); }
	public LuaValue   mod( double rhs )        { return LuaDouble.dmod(v,rhs); }
	public LuaValue   mod( int rhs )        { return LuaDouble.dmod(v,rhs); }
	public LuaValue   mod( long rhs )        { return LuaDouble.dmod(v,rhs); }
	public LuaValue   modFrom( double lhs )   { return LuaDouble.dmod(lhs,v); }
	public LuaValue   modFrom( int lhs )      { return LuaDouble.dmod(lhs,v); }
	public LuaValue   modFrom( long lhs )     { return LuaDouble.dmod(lhs,v); }
	
	public LuaValue   shl( LuaValue rhs )        { return rhs.shl(v); }
	public LuaValue   shl( int rhs )        { return valueOf(v << rhs); }
	public LuaValue   shl( double rhs )        { return LuaDouble.valueOf(v << (int)rhs); }
	public LuaValue   shl( long rhs )        { return valueOf(v << (int)rhs); }
	public LuaValue   shr( LuaValue rhs )        { return rhs.shr(v); }
	public LuaValue   shr( int rhs )        { return valueOf(v >> rhs); }
	public LuaValue   shr( double rhs )        { return LuaDouble.valueOf(v >> (int)rhs); }
	public LuaValue   shr( long rhs )        { return valueOf(v >> (int)rhs); }
	public LuaValue   band( LuaValue rhs )        { return rhs.band(v); }
	public LuaValue   band( int rhs )        { return valueOf(v & rhs); }
	public LuaValue   band( double rhs )        { return LuaDouble.valueOf(v & (int)rhs); }
	public LuaValue   band( long rhs )        { return valueOf(v & rhs); }
	public LuaValue   bor( LuaValue rhs )        { return rhs.bor(v); }
	public LuaValue   bor( int rhs )        { return valueOf(v | rhs); }
	public LuaValue   bor( double rhs )        { return LuaDouble.valueOf(v | (int)rhs); }
	public LuaValue   bor( long rhs )        { return valueOf(v | rhs); }
	public LuaValue   bxor( LuaValue rhs )        { return rhs.bxor(v); }
	public LuaValue   bxor( int rhs )        { return valueOf(v ^ rhs); }
	public LuaValue   bxor( double rhs )        { return LuaDouble.valueOf(v ^ (int)rhs); }
	public LuaValue   bxor( long rhs )        { return valueOf(v ^ rhs); }
	public LuaValue   bnot()                     { return valueOf(~v); }
	
	public LuaValue   lt( LuaValue rhs )         { return rhs instanceof LuaNumber ? (rhs.gt_b(v)? TRUE: FALSE) : super.lt(rhs); }
	public LuaValue   lt( double rhs )      { return v < rhs? TRUE: FALSE; }
	public LuaValue   lt( int rhs )         { return v < rhs? TRUE: FALSE; }
	public LuaValue   lt( long rhs )         { return v < rhs? TRUE: FALSE; }
	public boolean lt_b( LuaValue rhs )       { return rhs instanceof LuaNumber ? rhs.gt_b(v) : super.lt_b(rhs); }
	public boolean lt_b( int rhs )         { return v < rhs; }
	public boolean lt_b( double rhs )      { return v < rhs; }
	public boolean lt_b( long rhs )        { return v < rhs; }
	public LuaValue   lteq( LuaValue rhs )       { return rhs instanceof LuaNumber ? (rhs.gteq_b(v)? TRUE: FALSE) : super.lteq(rhs); }
	public LuaValue   lteq( double rhs )    { return v <= rhs? TRUE: FALSE; }
	public LuaValue   lteq( int rhs )       { return v <= rhs? TRUE: FALSE; }
	public LuaValue   lteq( long rhs )       { return v <= rhs? TRUE: FALSE; }
	public boolean lteq_b( LuaValue rhs )     { return rhs instanceof LuaNumber ? rhs.gteq_b(v) : super.lteq_b(rhs); }
	public boolean lteq_b( int rhs )       { return v <= rhs; }
	public boolean lteq_b( double rhs )    { return v <= rhs; }
	public boolean lteq_b( long rhs )      { return v <= rhs; }
	public LuaValue   gt( LuaValue rhs )         { return rhs instanceof LuaNumber ? (rhs.lt_b(v)? TRUE: FALSE) : super.gt(rhs); }
	public LuaValue   gt( double rhs )      { return v > rhs? TRUE: FALSE; }
	public LuaValue   gt( int rhs )         { return v > rhs? TRUE: FALSE; }
	public LuaValue   gt( long rhs )         { return v > rhs? TRUE: FALSE; }
	public boolean gt_b( LuaValue rhs )       { return rhs instanceof LuaNumber ? rhs.lt_b(v) : super.gt_b(rhs); }
	public boolean gt_b( int rhs )         { return v > rhs; }
	public boolean gt_b( double rhs )      { return v > rhs; }
	public boolean gt_b( long rhs )        { return v > rhs; }
	public LuaValue   gteq( LuaValue rhs )       { return rhs instanceof LuaNumber ? (rhs.lteq_b(v)? TRUE: FALSE) : super.gteq(rhs); }
	public LuaValue   gteq( double rhs )    { return v >= rhs? TRUE: FALSE; }
	public LuaValue   gteq( int rhs )       { return v >= rhs? TRUE: FALSE; }
	public LuaValue   gteq( long rhs )       { return v >= rhs? TRUE: FALSE; }
	public boolean gteq_b( LuaValue rhs )     { return rhs instanceof LuaNumber ? rhs.lteq_b(v) : super.gteq_b(rhs); }
	public boolean gteq_b( int rhs )       { return v >= rhs; }
	public boolean gteq_b( double rhs )    { return v >= rhs; }
	public boolean gteq_b( long rhs )      { return v >= rhs; }
	
	public int strcmp( LuaString rhs )      { typerror("attempt to compare number with string"); return 0; }
	
	public int checkint() {
		return (int) v;
	}
	public long checklong() {
		return v;
	}
	public double checkdouble() {
		return v;
	}
	public String checkjstring() {
		return String.valueOf(v);
	}
	public LuaString checkstring() {
		return valueOf( String.valueOf(v) );
	}

}