/* 18-842 Distributed Systems
 * Lab 2
 * Group 30 - aboos & dil1
 */

import java.io.Serializable;
import java.util.ArrayList;

public class LogMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	String event;
	TimeStamp eventTS; 
	ArrayList<LogMessage> nextMsgs = new ArrayList<LogMessage>();
	
	public LogMessage(String event, TimeStamp eventTs) 
	{
		this.event = event;
		this.eventTS = eventTs;
	}
	
	public LogMessage(LogMessage logMsg) 
	{
		this.event = logMsg.getEvent();
		this.eventTS = logMsg.getEventTS();
	}
	
	public String getEvent()
	{
		return this.event;
	}
	
	public TimeStamp getEventTS()
	{
		return this.eventTS;
	}
	
	public ArrayList<LogMessage> getNextMsgs()
	{
		return this.nextMsgs;
	}
	
	public String toString()
	{
		return ("Event: " + this.event + "TS: " + this.eventTS);
	}
}