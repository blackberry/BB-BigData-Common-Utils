package com.blackberry.bdp.common.utils.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import com.blackberry.bdp.common.utils.conversion.Converter;

public class Main
{	
	private static CuratorFramework curator;
	
	public static void main(String[] args) 
	{		
		try 
		{
			String action = args[0];
			String type = args[1];
			String zookeeperConnectionString = args[2];
			String zNodePath = args[3];

			System.out.printf("action=%s, type=%s, zookeeperConnectionString=%s, zNodePath=%s\n", 
					action, type, zookeeperConnectionString, zNodePath);			
			
			createCurator(zookeeperConnectionString);
						
			if (action.equals("get"))
			{
				zkGet(type, zNodePath);				
			}
			else if (action.equals("set"))
			{
				String zNodeVal = args[4];
				zkSet(type, zNodePath, zNodeVal);				
			}
			else
			{
				System.out.printf("error: unrecognized action: %s\n", action);
			}

		}
		catch (Throwable t)
		{
			System.out.printf("error: %s\n", t.getStackTrace().toString());
			t.printStackTrace();
		}
		
	}	

	private static void createCurator(String zookeeperConnectionString) 
	{
		String[] connStringAndPrefix = zookeeperConnectionString.split("/", 2);
		
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		
		Main.curator = CuratorFrameworkFactory.builder()
				.namespace(connStringAndPrefix[1])
				.connectString(connStringAndPrefix[0]).retryPolicy(retryPolicy)
				.build();				
		
		Main.curator.start();				
	}	
	
	private static void zkSet(String type, String zNodePath, String stringVal) throws Exception, UnsupportedTypeException
	{
		byte[] bytesVal = null;
		
		if (type.equals("long"))
		{
			long val = new Long(stringVal);
			bytesVal = Converter.getBytes(val);			
			System.out.printf("[set] %s=%d\n", zNodePath, val);
		}
		else if (type.equals("string"))
		{			
			System.out.printf("[set] %s=%s\n", zNodePath, stringVal);
			bytesVal = stringVal.getBytes();			
		}
		else
		{
			throw new UnsupportedTypeException("Unsupported type: type=" + type);
		}			
		
		if (Main.curator.checkExists().forPath(zNodePath) == null) 
		{
			Main.curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zNodePath, bytesVal);
		} else 
		{
			Main.curator.setData().forPath(zNodePath, bytesVal);
		}

	}
	
	private static void zkGet(String type, String zNodePath) throws Exception
	{
		if (Main.curator.checkExists().forPath(zNodePath) == null) 
		{
			System.out.printf("warning: there was no data at: %s:\n", zNodePath);
		} 
		else 
		{
			byte[] bytesVal = Main.curator.getData().forPath(zNodePath);

			if (type.equals("long"))
			{
				long longVal = Converter.longFromBytes(bytesVal);
				System.out.printf("[get] %s=%d\n", zNodePath, longVal);				
			}
			if (type.equals("string"))
			{
				String stringVal = new String(bytesVal);
				System.out.printf("[get] %s=%s\n", zNodePath, stringVal);
			}				
		}
		
	}

}
