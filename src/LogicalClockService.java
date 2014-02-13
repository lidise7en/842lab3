/* 18-842 Distributed Systems
 * Lab 2
 * Group 30 - aboos & dil1
 */

public class LogicalClockService extends ClockService {

	public LogicalClockService(TimeStamp newTS) {
		super(newTS);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void updateTS(TimeStamp ts) {
		// TODO Auto-generated method stub
		if(ts.getLamportClock() > this.getTs().getLamportClock()) {
			this.setTs(ts);
		}
		
	}
	
	public void addTS(String srcName) {
		TimeStamp ts = this.getTs();
		ts.setLamportClock(ts.getLamportClock() + 1);
	}
}
