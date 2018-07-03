/**
 * 
 */
package cn.pdd.component.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 树节点
 * @author paddingdun
 *
 *         2018年7月3日
 * @since 1.0
 * @version 1.0
 */
public class TreeNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 节点id;
	 */
	private String nodeId;

	/**
	 * 父节点id;
	 */
	private String parentId;

	/**
	 * 节点名称;
	 */
	private String nodeName;

	/**
	 * 是否选中;
	 */
	private boolean checked;

	// /**
	// * 是否是父节点;
	// */
	// private boolean isParent;

	/**
	 * 用于菜单是否显示;
	 */
	private boolean show = true;

	/**
	 * 用于显示名称
	 */
	private String title;

	/**
	 * 附加信息;
	 */
	private Object object;

	/**
	 * 儿子集合;
	 */
	private List<TreeNode> children = new ArrayList<TreeNode>();

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public List<TreeNode> getChildren() {
		return children;
	}

	/**
	 * 添加儿子节点;
	 * 
	 * @param treeNode
	 */
	public void addChild(TreeNode treeNode) {
		this.children.add(treeNode);
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	// public boolean isIsParent() {
	// return isParent;
	// }
	//
	//
	// public void setIsParent(boolean isParent) {
	// this.isParent = isParent;
	// }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TreeNode other = (TreeNode) obj;
		if (nodeId == null) {
			if (other.nodeId != null)
				return false;
		} else if (!nodeId.equals(other.nodeId))
			return false;
		return true;
	}

	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
