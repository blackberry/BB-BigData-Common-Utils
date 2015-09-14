package com.blackberry.bdp.common.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import com.blackberry.bdp.common.conversion.Converter;
import java.util.Arrays;

public class ResetAllOffsets {

	private static CuratorFramework curator;

	public static void main(String[] args) {
		try {
			String zookeeperConnectionString = args[0];
			createCurator(zookeeperConnectionString);			
			for (String topic : curator.getChildren().forPath("/kaboom/topics")) {
				for (String partition : curator.getChildren().forPath("/kaboom/topics/" + topic)) {
					String offsetPath = String.format("%s/%s/%s", "/kaboom/topics/", topic, partition);
					if (curator.checkExists().forPath(offsetPath) != null) {
						int offset = Converter.intFromBytes(curator.getData().forPath(offsetPath));
						System.out.println("Found " + offsetPath + "=" +offset);
					} else {
						System.out.println("Missing offset at " + offsetPath);
					}						
				}
			}
		} catch (Throwable t) {
			System.out.printf("error: %s\n", t.getStackTrace().toString());
			t.printStackTrace();
		}

	}

	private static void createCurator(String zookeeperConnectionString) {
		String[] connStringAndPrefix = zookeeperConnectionString.split("/", 2);

		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

		ResetAllOffsets.curator = CuratorFrameworkFactory.builder()
			 .namespace(connStringAndPrefix[1])
			 .connectString(connStringAndPrefix[0]).retryPolicy(retryPolicy)
			 .build();

		ResetAllOffsets.curator.start();
	}

}
