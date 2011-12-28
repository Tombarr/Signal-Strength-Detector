package com.tombarrasso.android.signaldetector;

import java.lang.reflect.*;
import android.util.Log;

public final class ReflectionUtils
{
	public static final String TAG = ReflectionUtils.class.getSimpleName();
	
	/**
	 * Dumps a {@link Class}'s {@link Method}s and {@link Field}s
	 * as a String.
	 */
	public static final String dumpClass(Class<?> mClass, Object mInstance)
	{
		if (mClass == null || mInstance == null) return null;
		
		String mStr = mClass.getSimpleName() + "\n\n";
		
		mStr += "FIELDS\n\n";
		
		final Field[] mFields = mClass.getDeclaredFields();
		
		for (final Field mField : mFields)
		{
			mField.setAccessible(true);
		
			mStr += mField.getName() + " (" + mField.getType() + ") = ";
			
			try
			{
				mStr += mField.get(mInstance).toString();
			}
			catch (Exception e)
			{
				mStr += "null";
				Log.e(TAG, "Could not get Field `" + mField.getName() + "`.", e);
			}
			
			mStr += "\n";
		}
		
		mStr += "METHODS\\nn";
		
		// Dump all methods.
		
		final Method[] mMethods = mClass.getMethods();
		
		for (final Method mMethod : mMethods)
		{
			mMethod.setAccessible(true);
		
			mStr += mMethod.getReturnType() + " " + mMethod.getName() + "() = ";
			
			try
			{
				final Object mRet = mMethod.invoke(mInstance);
				mStr += (mRet == null) ? "null" : mMethod.invoke(mInstance).toString();
			}
			catch (Exception e)
			{
				mStr += "null";
				Log.e(TAG, "Could not get Method `" + mMethod.getName() + "`.", e);
			}
			
			mStr += "\n";
		}
		
		return mStr;
	}
	
	/**
	 * @return A string containing the values of all static {@link Field}s.
	 */
	public static final String dumpStaticFields(Class<?> mClass, Object mInstance)
	{
		if (mClass == null || mInstance == null) return null;
		
		String mStr = mClass.getSimpleName() + "\n\n";
		
		mStr += "STATIC FIELDS\n\n";
		
		final Field[] mFields = mClass.getDeclaredFields();
		
		for (final Field mField : mFields)
		{
			if (ReflectionUtils.isStatic(mField))
			{
				mField.setAccessible(true);
			
				mStr += mField.getName() + " (" + mField.getType() + ") = ";
				
				try
				{
					mStr += mField.get(mInstance).toString();
				}
				catch (Exception e)
				{
					mStr += "null";
					Log.e(TAG, "Could not get Field `" + mField.getName() + "`.", e);
				}
				
				mStr += "\n";
			}
		}
		
		return mStr;
	}
	
	/**
	 * @return True if the {@link Field} is static.
	 */
	public final static boolean isStatic(Field field)
	{
    	final int modifiers = field.getModifiers();
    	return (Modifier.isStatic(modifiers));
    }
}