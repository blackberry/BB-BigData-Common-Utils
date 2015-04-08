/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blackberry.bdp.common.matching;

import java.io.UnsupportedEncodingException;
import static java.lang.Math.min;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Arrays.sort;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringMatcher
{
	List<SortedKeyword> keywords = new ArrayList<>();
		
	int textIndex;
	int keywordIndex;
	int keywordNum;
	int keywordMatchIndex;
	int maxKeywordLength = 0;
	
	SortedKeyword currentKeyword;
		
	Logger LOG = LoggerFactory.getLogger(StringMatcher.class);
	
	public StringMatcher(String[] keywordStrings) throws Exception
	{
		sort(keywordStrings);
		
		for (keywordNum = 0; keywordNum < keywordStrings.length; keywordNum++)
		{
			if ( keywordStrings[keywordNum].length() > maxKeywordLength)
			{
				maxKeywordLength = keywordStrings[keywordNum].length();
			}
			
			int startingUniqueOffset = 0;
			byte[] bytes = keywordStrings[keywordNum].getBytes(Charset.forName("UTF-8"));

			// Compare  keywords to the previous in the sort order and 
			// determine the position of the first non-matching character
			
			if (keywordNum > 0)
			{
				if (keywordStrings[keywordNum].equals(keywordStrings[keywordNum - 1]))
				{
					throw new Exception("Can't have duplicate keywords");						 
				}
				
				for (int charNum = 0; charNum < keywordStrings[keywordNum - 1].length(); charNum++)
				{
					if(bytes[charNum] == keywords.get(keywordNum - 1).bytes[charNum])
					{
						startingUniqueOffset = charNum + 1;
					}
					else
					{
						break;
					}
				}
				LOG.debug("Setting keyword number {} with a next starting offset of {}", keywordNum - 1, startingUniqueOffset);
				keywords.get(keywordNum - 1).nextUniqueStartingOffset = startingUniqueOffset;
			}
			
			keywords.add(new SortedKeyword(keywordStrings[keywordNum], bytes));
		}
		
		LOG.debug("Max keyword length is {}", maxKeywordLength);
		
		// All the sorted keywords up to N-1 have their nextUniqueStartingOffset, set the last one's to zero
		keywords.get(keywords.size() - 1).nextUniqueStartingOffset = 0;
		
	}
	
	public boolean utf8ByteArrayContainsAnyKeyword(byte[] bytes) throws UnsupportedEncodingException
	{
		textIndex = 0;		
		keywordIndex = 0;

		while(textIndex < bytes.length - maxKeywordLength)
		{			
			keywordNum = 0;
						
			while (keywordNum < keywords.size())
			{
				currentKeyword = keywords.get(keywordNum);
				
				while(keywordIndex < currentKeyword.bytes.length)
				{
					String textSubset = new String(Arrays.copyOfRange(bytes, textIndex, textIndex + keywordIndex + 1), "UTF-8");
					String keywordSubset = new String(Arrays.copyOfRange(currentKeyword.bytes, 0, keywordIndex + 1), "UTF-8");
					
					if (bytes[textIndex + keywordIndex] != currentKeyword.bytes[keywordIndex])
					{
						LOG.trace("keywordNum: {}, keywordIndex: {}, textIndex: {}, textSubset {} does not match keywordSubset: {}", 
							 keywordNum, keywordIndex, textIndex, textSubset, keywordSubset);
						break;						
					}
					
					LOG.trace("keywordNum: {}, keywordIndex: {}, textIndex: {}, textSubset {} matches keywordSubset: {}", 
						 keywordNum, keywordIndex, textIndex, textSubset, keywordSubset);
					
					if (keywordIndex == currentKeyword.bytes.length - 1)
					{	
						return true;
					}
					
					keywordIndex++;
				}
				
				keywordIndex = min(keywordIndex, currentKeyword.nextUniqueStartingOffset);				
				keywordNum++;
			}
			
			textIndex++;
		}		
		
		return false;
	}
	
	private class SortedKeyword
	{		
		private final String keyword;
		private final byte[] bytes;				
		private int nextUniqueStartingOffset; 
		
		private SortedKeyword(String keyword, byte[] bytes)
		{
			this.keyword = keyword;
			this.bytes = bytes;			
		}
	}	
}
