/* 18-842 Distributed Systems
 * Lab 2
 * Group 30 - aboos & dil1
 */

import java.io.Serializable;


public class NackItem implements Serializable {
	
	private static final long serialVersionUID = 1L;


	SrcGroup srcGrp;
	int seqNum;
	
	public NackItem(SrcGroup srcGrp, int seqNum) {
		this.srcGrp = srcGrp;
		this.seqNum = seqNum;
	}
	
	public SrcGroup getSrcGrp() {
		return srcGrp;
	}
	
	public int getSeqNum(){
		return seqNum;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + seqNum;
		result = prime * result + ((srcGrp == null) ? 0 : srcGrp.hashCode());
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
		NackItem other = (NackItem) obj;
		if (seqNum != other.seqNum)
			return false;
		if (srcGrp == null) {
			if (other.srcGrp != null)
				return false;
		} else if (!srcGrp.equals(other.srcGrp))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "NackItem [srcGrp=" + srcGrp + ", seqNum=" + seqNum + "]";
	}
}
