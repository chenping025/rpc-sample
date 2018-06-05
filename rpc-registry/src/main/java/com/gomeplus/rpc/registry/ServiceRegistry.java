package com.gomeplus.rpc.registry;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 连接zk,并注册服务
 */
public class ServiceRegistry {

	private static final Logger log = LoggerFactory.getLogger(ServiceRegistry.class);
	
	private String registryAddress;
	
	private CountDownLatch countDownLatch = new CountDownLatch(1);
	
	public ServiceRegistry(String registryAddress) {
		this.registryAddress = registryAddress;
	}
	
	/**
	 * 向zk注册服务，并监听
	 */
	public void registry(String data) {
		if (null != data) {
			ZooKeeper zk = connectServer();
			if (null != zk) {
				createNode(zk, data);
			}
		}
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

	/**
	 * 创建节点
	 * @param zk
	 * @param data
	 */
	private void createNode(ZooKeeper zk, String data) {
		try {
			if (null == zk.exists(Constant.ZK_REGISTRY_PATH, false)) {
				zk.create(Constant.ZK_REGISTRY_PATH, null, Ids.OPEN_ACL_UNSAFE, 
						CreateMode.PERSISTENT);
			}
			
			String path = zk.create(Constant.ZK_DATA_PATH, data.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			log.debug("create zk node ({} -> {})", path, data);
		} catch (Exception e) {
			log.error("create zk node caught exception", e);
		} 
	}
}
