/* 18-842 Distributed Systems
 * Lab 2
 * Group 30 - aboos & dil1
 */

public class TimeStampedMessage extends Message {

	private static final long serialVersionUID = 1L;
	private TimeStamp msgTS = null;
	private int grpSeqNum = 0;
	
	public TimeStampedMessage(String dest, String kind, Object data, TimeStamp ts, String src) {
		super(dest, kind, data, src);
		this.msgTS = ts;
		// TODO Auto-generated constructor stub
	}

	public TimeStamp getMsgTS() {
		return msgTS;
	}

	public String getMsg() {
		return super.toString();
	}
	
	public void setMsgTS(TimeStamp msgTS) {
		this.msgTS = msgTS;
	}
	
	public void setGrpSeqNum(int grpSeqNum) {
		this.grpSeqNum = grpSeqNum;
	}
	
	public int getGrpSeqNum() {
		return grpSeqNum;
	}
	
	public Message makeCopy() {
		Message result = new TimeStampedMessage(this.getDest(), this.getKind(), this.getData(), this.msgTS, this.getSrc());
		result.set_source(this.getSrc());
		result.set_duplicate(this.isDuplicate());
		result.set_seqNum(this.getSeqNum());
		result.setGrpDest(this.getGrpDest());
		((TimeStampedMessage)result).setGrpSeqNum(this.grpSeqNum);
		return result;
	}
	
	public void dumpMsg(){
		System.out.println("Src: " + super.getSrc());
		System.out.println("Dest: " + super.getDest());
		System.out.println("Kind: " + super.getKind());
		System.out.println("Sequence Number: " + super.getSeqNum());
		System.out.println("Data: " + super.getData());
		System.out.println("Group Sequence Number: " + this.grpSeqNum);
		//System.out.println("Msg: TS:  " + msgTS.toString());
	}

	public String toString() {
		return (super.toString() + msgTS.toString() + this.grpSeqNum); 
	}
}
