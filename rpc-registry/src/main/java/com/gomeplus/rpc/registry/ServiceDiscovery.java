package com.gomeplus.rpc.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceDiscovery {
	
private static final Logger log = LoggerFactory.getLogger(ServiceRegistry.class);
	
	private String registryAddress;
	
	private CountDownLatch countDownLatch = new CountDownLatch(1);
	
	private volatile List<String> dataList = new ArrayList<>();
	
	public ServiceDiscovery(String registryAddress) {
		this.registryAddress = registryAddress;
		
		ZooKeeper zk = connectServer();
		if (null != zk) {
			watchNode(zk);
		}
	}
	
	/**
	 * 获取节点值
	 */
	public String discovery() {
		String data = null;
		int size = dataList.size();
		if (size > 0) {
			if (size == 1) {
				data = dataList.get(0);
				log.debug("using only data: {}", data);
			} else {
				data = dataList.get(ThreadLocalRandom.current().nextInt(size));
				log.debug("using random data: {}", data);
			}
		}
		
		return data;
	}

	/**
	 * 创建zk连接，并监听
	 * @return
	 */
	private ZooKeeper connectServer() {
		ZooKeeper zk = null;
		try {
			zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, 
				new Watcher() {
				
					@Override
					public void process(WatchedEvent event) {
						if (Event.KeeperState.SyncConnected.equals(event.getState())) {
							countDownLatch.countDown();
						}
					}
				});
			countDownLatch.await();
		} catch (Exception e) {
			log.error("Connect zk server caught exception", e);
		} 
		return zk;
	}
	
	private void watchNode(final ZooKeeper zk) {
		try {
			List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
				
				@Override
				public void process(WatchedEvent event) {
					//如果子节点发生改变，重新获取服务地址
					if (EventType.NodeChildrenChanged.equals(event.getType())) {
						watchNode(zk);
					}
				}
			});
			
			List<String> dataList = new ArrayList<>();
			
			for(String node : nodeList) {
				//获取节点对应的value值，false不监听value值变化事件类型
				byte[] data = zk.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, new Stat());
				dataList.add(new String(data));
			}
			log.debug("zk node data:{}", dataList);
			this.dataList = dataList;
		} catch (Exception e) {
			log.error("watch zk node caught exception", e);
		}
		
	}

}
