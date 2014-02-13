/* 18-842 Distributed Systems
 * Lab 2
 * Group 30 - aboos & dil1
 */

import java.io.Serializable;


public class SrcGroup implements Serializable {


	private static final long serialVersionUID = 1L;
	private String src;
	private String groupName;
	
	public SrcGroup(String source, String group) {
		this.src = source;
		this.groupName = group;
	}
	
	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getGroupName() {
		return groupName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((groupName == null) ? 0 : groupName.hashCode());
		result = prime * result + ((src == null) ? 0 : src.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SrcGroup other = (SrcGroup) obj;
		if (groupName == null) {
			if (other.groupName != null)
				return false;
		} else if (!groupName.equals(other.groupName))
			return false;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		return true;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@Override
	public String toString() {
		return "SrcGroup [src=" + src + ", groupName=" + groupName + "]";
	}
	
}
