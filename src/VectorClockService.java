/* 18-842 Distributed Systems
 * Lab 2
 * Group 30 - aboos & dil1
 */

import java.util.HashMap;


public class VectorClockService extends ClockService {

	public VectorClockService(TimeStamp newTS) {
		super(newTS);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void updateTS(TimeStamp ts) {
		// TODO Auto-generated method stub
		HashMap<String, Integer> mapOne = ts.getVectorClock();
		HashMap<String, Integer> mapTwo = this.getTs().getVectorClock();
		for(String e : ts.getVectorClock().keySet()) {
			if(mapOne.get(e) > mapTwo.get(e)) {
				mapTwo.put(e, mapOne.get(e));
			}
		}
	}

	public void addTS(String srcName) {
		HashMap<String, Integer> map = this.getTs().getVectorClock();
		if(map.containsKey(srcName))
			map.put(srcName, map.get(srcName) + 1); 
		else {
			System.out.println("Cannot find this host's timestamp");
		}
	}
}
