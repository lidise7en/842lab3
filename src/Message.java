/* 18-842 Distributed Systems
 * Lab 2
 * Group 30 - aboos & dil1
 */

import java.io.Serializable;

public abstract class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private String src;
	private String dest;
	private String kind;
	private Object data;
	private boolean duplicate;
	private int seqNum;

	private String grpDest;
	
	public Message(String dest, String kind, Object data, String src) {
		this.dest = dest;
		this.kind = kind;
		this.data = data;
		this.duplicate = false;
		this.grpDest = null;
		this.src = src;
	}



	// These settors are used by MessagePasser.send, not your app
	public void set_source(String source) {
		this.src = source;
	}
	public void set_seqNum(int sequenceNumber) {
		this.seqNum = sequenceNumber;
	}
	public void set_duplicate(Boolean dupe) {
		this.duplicate = dupe;
	}
	// other accessors, toString, etc as needed
	public void setDest(String destination) {
		this.dest = destination;
	}
	public String getDest() {
		return dest;
	}
	public String getKind() {
		return kind;
	}
	public Object getData() {
		return data;
	}
	public boolean isDuplicate() {
		return duplicate;
	}
	public int getSeqNum() {
		return seqNum;
	}
	public String getSrc() {
		return src;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((dest == null) ? 0 : dest.hashCode());
		result = prime * result + (duplicate ? 1231 : 1237);
		result = prime * result + ((grpDest == null) ? 0 : grpDest.hashCode());
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + seqNum;
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
		Message other = (Message) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (dest == null) {
			if (other.dest != null)
				return false;
		} else if (!dest.equals(other.dest))
			return false;
		if (duplicate != other.duplicate)
			return false;
		if (grpDest == null) {
			if (other.grpDest != null)
				return false;
		} else if (!grpDest.equals(other.grpDest))
			return false;
		if (kind == null) {
			if (other.kind != null)
				return false;
		} else if (!kind.equals(other.kind))
			return false;
		if (seqNum != other.seqNum)
			return false;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		return true;
	}



	@Override
	public String toString() {
		return "Message [src=" + src + ", dest=" + dest + ", kind=" + kind
				+ ", data=" + data + ", duplicate=" + duplicate + ", seqNum="
				+ seqNum + ", grpDest=" + grpDest + "]";
	}



	public String getGrpDest() {
		return grpDest;
	}

	public void setGrpDest(String grpDest) {
		this.grpDest = grpDest;
	}
	

	public abstract Message makeCopy();
}
