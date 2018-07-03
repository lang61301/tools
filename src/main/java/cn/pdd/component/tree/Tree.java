/**
 * 
 */
package cn.pdd.component.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cn.pdd.util.json.GsonHelper;

/**
 * 抽象树
 * @author paddingdun
 *
 *         2018年7月3日
 * @since 1.0
 * @version 1.0
 */
public abstract class Tree {

	/**
	 * Tree 日志变量;
	 */

	private final static Logger logger = Logger.getLogger(Tree.class);

	private static ReentrantLock lock = new ReentrantLock();

	/**
	 * 
	 */
	private Map<String, TreeNode> map_nodes = new HashMap<String, TreeNode>();

	private List<TreeNode> rootNodes = new ArrayList<TreeNode>();

	/**
	 * 用于提供rootNodes副本;防止修改了TreeNode记录;
	 */
	private String rootNodesJson = null;

	public void init() {
		operate();
	}

	private void operate() {
		lock.lock();
		try {
			// 获取节点集合;
			List<TreeNode> list_nodes = transform();

			map_nodes.clear();
			rootNodes.clear();

			rootNodesJson = null;

			// 将list转成map;
			for (TreeNode tn : list_nodes) {
				map_nodes.put(tn.getNodeId(), tn);
			}

			Gson gson = GsonHelper.create();
			logger.debug(gson.toJson(map_nodes));
			// 处理树;
			for (TreeNode tn : list_nodes) {
				// logger.debug(tn.getNodeId());
				if (isRoot(tn)) {
					rootNodes.add(tn);
				} else {
					String parentId = tn.getParentId();
					// logger.debug(parentId);
					if (parentId != null) {
						TreeNode parentNode = map_nodes.get(parentId);
						if (parentNode != null) {
							parentNode.addChild(tn);
						} else {
							logger.error(String.format("parentId[%s]未查到父节点，树异常", parentId));
						}
					} else {
						logger.error("parentId is null and is not root!");
					}
				}
			}

			// 同级节点排序;
			sort(rootNodes);
			for (TreeNode tn : list_nodes) {
				List<TreeNode> children = tn.getChildren();
				if (!children.isEmpty()) {
					sort(children);
				}
			}

			rootNodesJson = gson.toJson(rootNodes);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 刷新树;
	 */
	public void refreshTree() {
		operate();
	}

	/**
	 * 获取副本;
	 * 
	 * @return
	 */
	public List<TreeNode> getRootNodes() {
		return getRootNodes(true);
	}

	/**
	 * 获取根节点集合; copy:true表明获取的是副本,可以随意修改; copy:false表示获取是原本,注意修该;
	 * 
	 * @param copy
	 * @return
	 */
	public List<TreeNode> getRootNodes(boolean copy) {
		if (copy) {
			Gson gson = GsonHelper.create();
			List<TreeNode> cp = Collections.emptyList();
			if (rootNodesJson != null) {
				cp = gson.fromJson(rootNodesJson, new TypeToken<List<TreeNode>>() {
				}.getType());
			}
			return cp;
		} else {
			return this.rootNodes;
		}
	}

	// /**
	// * 获取树节点;
	// * @param nodeId
	// * @return
	// */
	// public TreeNode getTreeNode(String nodeId){
	// return map_nodes.get(nodeId);
	// }

	abstract public List<TreeNode> transform();

	abstract public boolean isRoot(TreeNode treeNode);

	abstract public void sort(List<TreeNode> children);
}
