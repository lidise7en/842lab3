/* 18-842 Distributed Systems
 * Lab 2
 * Group 30 - aboos & dil1
 */

public class Rule {
	private String action;
	private String src;
	private String dest;
	private String kind;
	private int seqNum;
	private String duplicate;
	
	public Rule() {
		action = "";
		src = "";
		dest = "";
		kind = "";
		seqNum = -1;
		duplicate = "";
	}
	
	public String getAction() {
		return action;
	}


	public void setAction(String action) {
		this.action = action;
	}


	public String getSrc() {
		return src;
	}


	public void setSrc(String src) {
		this.src = src;
	}


	public String getDest() {
		return dest;
	}


	public void setDest(String dest) {
		this.dest = dest;
	}


	public String getKind() {
		return kind;
	}


	public void setKind(String kind) {
		this.kind = kind;
	}


	public int getSeqNum() {
		return seqNum;
	}


	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}


	public String getDuplicate() {
		return duplicate;
	}


	public void setDuplicate(String duplicate) {
		this.duplicate = duplicate;
	}


	@Override
	public String toString() {
		return "Rule [action=" + action + ", src=" + src + ", dest=" + dest
				+ ", kind=" + kind + ", seqNum=" + seqNum + ", duplicate="
				+ duplicate + "]";
	}
	
	
}
