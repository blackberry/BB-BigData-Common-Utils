/**
 * Copyright 2014 BlackBerry, Limited.
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

package com.blackberry.bdp.common.conversion;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Converter 
{
	
	private static final Logger LOG = LoggerFactory.getLogger(Converter.class);
	
	/*
	 * Creates a long from a byte array and an offset (GC friendly) 
	 */
	
	public static long longFromBytes(byte[] data, int offset) {
		return ((long) data[offset] & 0xFFL) << 56 //
				| ((long) data[offset + 1] & 0xFFL) << 48 //
				| ((long) data[offset + 2] & 0xFFL) << 40 //
				| ((long) data[offset + 3] & 0xFFL) << 32 //
				| ((long) data[offset + 4] & 0xFFL) << 24 //
				| ((long) data[offset + 5] & 0xFFL) << 16 //
				| ((long) data[offset + 6] & 0xFFL) << 8 //
				| ((long) data[offset + 7] & 0xFFL);

	}
	
	/*
	 * Creates a long from a byte array 
	 */
	
	public static long longFromBytes(byte[] data) {
		return longFromBytes(data, 0);
	}

	/*
	 * Creates a byte array from a long  
	 */

	public static byte[] getBytes(long l) 
	{
		return new byte[] //
		{ (byte) (l >> 56), //
				(byte) (l >> 48), //
				(byte) (l >> 40),//
				(byte) (l >> 32), //
				(byte) (l >> 24),//
				(byte) (l >> 16),//
				(byte) (l >> 8),//
				(byte) (l) //
		};
	}
	
	public static int intFromBytes(byte[] bytes) {
		return bytes[0] << 24 
			 | (bytes[1] & 0xFF) << 16 
			 | (bytes[2] & 0xFF) << 8 
			 | (bytes[3] & 0xFF);
	}
	
	/**
	 * Creates a byte array from an int
	 * @param i
	 * @return
	 */
	public static byte[] getBytes(int i) {
		return new byte[]
		{(byte) (i >> 24),
			(byte) (i >> 16),
			(byte) (i >> 8),
			(byte) (i /*>> 0*/) //
		};
	}
	
	/*
	 * Takes a timestamp and a string template containing date symbols 
	 * Creates a calendar object from the timestamp 
	 * Returns a string of the template populated with date attributes
	 *  
	 * Supported date symbols and their replacements:
	 * 
	 * %y: Calendar.YEAR
	 * %M: Calendar.MONTH + 1
	 * %d: Calendar.DAY_OF_MONTH
	 * %H: Calendar.HOUR_OF_DAY
	 * %m: Calendar.MINUTE
	 * %s: Calendar.SECOND
	 * %l: The system's host name
	 *		 
	 */
	public static String timestampTemplateBuilder(long timestamp, String template) 
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(timestamp);

		long templateLength = template.length();
		
		String hostname;
		
		try 
		{
			hostname = InetAddress.getLocalHost().getCanonicalHostName();
		} 
		catch (UnknownHostException e) 
		{
			LOG.error("Can't determine local hostname");
			hostname = "unknown.host";
		}

		StringBuilder sb = new StringBuilder();
		int i = 0;
		int p = 0;
		char c;
		
		while (true) 
		{
			p = template.indexOf('%', i);
			
			if (p == -1) 
			{
				sb.append(template.substring(i));
				break;
			}
			
			sb.append(template.substring(i, p));

			if (p + 1 < templateLength) 
			{
				c = template.charAt(p + 1);
				
				switch (c) {
				case 'y':
					sb.append(String.format("%04d", cal.get(Calendar.YEAR)));
					break;
				case 'M':
					sb.append(String.format("%02d", cal.get(Calendar.MONTH) + 1));
					break;
				case 'd':
					sb.append(String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)));
					break;
				case 'H':
					sb.append(String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)));
					break;
				case 'm':
					sb.append(String.format("%02d", cal.get(Calendar.MINUTE)));
					break;
				case 's':
					sb.append(String.format("%02d", cal.get(Calendar.SECOND)));
					break;
				case 'l':
					sb.append(hostname);
					break;
				default:
					sb.append('%').append(c);
				}
			} 
			else 
			{
				sb.append('%');
				break;
			}

			i = p + 2;

			if (i >= templateLength) {
				break;
			}
		}

		return sb.toString();
	}

}
