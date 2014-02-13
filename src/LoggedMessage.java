/* 18-842 Distributed Systems
 * Lab 2
 * Group 30 - aboos & dil1
 */

import java.util.ArrayList;



public class LoggedMessage {
	
	TimeStampedMessage msg;
	ArrayList<LoggedMessage> nextMsgs = new ArrayList<LoggedMessage>();
	
	public LoggedMessage(TimeStampedMessage inputMsg) 
	{
		this.msg = inputMsg;
	}
	
	public TimeStampedMessage getTSMsg()
	{
		return this.msg;
	}
	
	public ArrayList<LoggedMessage> getNextMsgs()
	{
		return this.nextMsgs;
	}
	
	public void dumpLoggedMsg()
	{
		System.out.println("\t\tMSG DUMP START\n");
		
		msg.dumpMsg();
		
		System.out.println("\t\t\t NextMSG DUMP START\n");
		for (LoggedMessage nextMsg : nextMsgs) {
			nextMsg.dumpLoggedMsg();
		}
		System.out.println("\t\t\t NextMSG DUMP END\n");
		
		System.out.println("\t\tMSG DUMP END\n");
	}
	
	public String toString()
	{
		return (this.msg.getMsgTS().toString() + this.msg.getData().toString());
	}
}
