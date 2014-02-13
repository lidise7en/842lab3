/* 18-842 Distributed Systems
 * Lab 2
 * Group 30 - aboos & dil1
 */

import java.io.Serializable;
import java.util.HashMap;


public class TimeStamp implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int lamportClock;
	private HashMap<String, Integer> vectorClock = null;
	
	public TimeStamp() {
		this.lamportClock = 0;
		this.vectorClock = new HashMap<String, Integer>();
	}

	public int getLamportClock() {
		return lamportClock;
	}

	public void setLamportClock(int lamportClock) {
		this.lamportClock = lamportClock;
	}

	public HashMap<String, Integer> getVectorClock() {
		return vectorClock;
	}

	public void setVectorClock(HashMap<String, Integer> vectorClock) {
		this.vectorClock = vectorClock;
	}
	
	public TimeStampRelation compare(TimeStamp ts) {

		if(this.vectorClock.size() == 0) {//this is a lamport timestamp
			if(this.lamportClock == ts.lamportClock) {
				return TimeStampRelation.equal;
			}
			else if(this.lamportClock > ts.lamportClock) {
				return TimeStampRelation.greaterEqual;
			}
			else {
				return TimeStampRelation.lessEqual;
			}
		}
		else {//this is a vector timestamp
			int flag = 0;
			for(String e : this.vectorClock.keySet()) {
				int num1 = this.vectorClock.get(e);
				int num2 = ts.getVectorClock().get(e);
				if(num1 == num2) {
					continue;
				}
				else if(num1 > num2) {
					if(flag == -1) {
						return TimeStampRelation.concurrent;
					}
					else if(flag == 0){
						flag = 1;
					}
					else {
						continue;
					}
				}
				else {
					if(flag == 1) {
						return TimeStampRelation.concurrent;
					}
					else if(flag == 0) {
						flag = -1;
					}
					else {
						continue;
					}
				}
			}
			if(flag == 0)
				return TimeStampRelation.equal;
			else if(flag == 1) {
				return TimeStampRelation.greaterEqual;
			}
			else {
				return TimeStampRelation.lessEqual;
			}
		}
		
	}
	@SuppressWarnings("unchecked")
	public TimeStamp makeCopy() {
		TimeStamp result = new TimeStamp();
		int tmp = this.lamportClock;
		result.setLamportClock(tmp);
		result.setVectorClock((HashMap<String, Integer>)this.vectorClock.clone());
		return result;
	}
	@Override
	public String toString() {
		return "TimeStamp [lamportClock=" + lamportClock + ", vectorClock="
				+ vectorClock + "]";
	}
}