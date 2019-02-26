package com.flk.epubviewersample.util;

import android.util.Log;

import com.flk.epubviewersample.data.Define;

import java.io.PrintWriter;
import java.io.StringWriter;


public class LogUtil {

	public static boolean logable() {		
		return Define.DEBUG_LOG_CHECK;
	}

	public static void d(String className, String logMsg, Throwable tr) {
		if (logable()) {
			StringBuffer buf = new StringBuffer() ;
			buf.append( logMsg );
			buf.append( '\n' ) ;
			buf.append( getStackTraceString(tr) );			
			d(className, buf.toString() );
		}
	}	

	public static void d(String className, String logMsg) {
		if (logable()) {
			String classLog = "[" + className + "] ";
			Log.d(Define.LOGTAG, classLog + logMsg);
		}
	}	

	public static void i(String className, String logMsg, Throwable tr) {
		if (logable()) {
			StringBuffer buf = new StringBuffer() ;
			buf.append( logMsg );
			buf.append( '\n' ) ;
			buf.append( getStackTraceString(tr) );			
			i(className, buf.toString() );
		}
	}	

	public static void i(String className, String logMsg) {
		if (logable()) {
			String classLog = "[" + className + "] ";
			Log.i(Define.LOGTAG, classLog + logMsg);
		}
	}

	public static void v(String className, String logMsg, Throwable tr) {
		if (logable()) {
			StringBuffer buf = new StringBuffer() ;
			buf.append( logMsg );
			buf.append( '\n' ) ;
			buf.append( getStackTraceString(tr) );			
			v(className, buf.toString() );
		}
	}

	public static void v(String className, String logMsg) {
		if (logable()) {
			String classLog = "[" + className + "] ";
			Log.v(Define.LOGTAG, classLog + logMsg);
		}
	}

	public static void w(String className, String logMsg, Throwable tr) {
		if (logable()) {
			StringBuffer buf = new StringBuffer() ;
			buf.append( logMsg );
			buf.append( '\n' ) ;
			buf.append( getStackTraceString(tr) );			
			w(className, buf.toString() );
		}
	}	

	public static void w(String className, String logMsg) {
		if (logable()) {
			String classLog = "[" + className + "] ";
			Log.w(Define.LOGTAG, classLog + logMsg);
		}
	}

	public static void e(String className, String logMsg, Throwable tr) {
		StringBuffer buf = new StringBuffer() ;
		buf.append( logMsg );
		buf.append( '\n' ) ;
		buf.append( getStackTraceString(tr) );			
		e(className, buf.toString() );
	}

	public static void e(String className, String logMsg) {
		//if (logable) 
		{
			String classLog = "[" + className + "] ";
			Log.e(Define.LOGTAG, classLog + logMsg);
		}
	}

	/**
	 * Handy function to get a loggable stack trace from a Throwable
	 * @param tr An exception to log
	 */
	public static String getStackTraceString(Throwable tr) {
		if (tr == null) {
			return "";
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		tr.printStackTrace(pw);
		return sw.toString();
	}	
}

