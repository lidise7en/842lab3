/* 18-842 Distributed Systems
 * Lab 2
 * Group 30 - aboos & dil1
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Iterator;


public class Logger implements Runnable {
	/*
	 * Command Type:
	 * quit: quit the whole process
	 * ps: print the information of current MessagePasser
	 * send command: dest kind data
	 * receive command: receive
	 */
	private MessagePasser msgPasser; 
	HashMap<String, ArrayList<LogMessage>> eventMap = new HashMap<String, ArrayList<LogMessage>>();
	private ArrayList<LogMessage> startMsgs = new ArrayList<LogMessage>();
	boolean isLogical;
	
	private Logger(MessagePasser msgPasser) {
		Thread receiverThread;
		this.msgPasser = msgPasser;
		this.isLogical = msgPasser.getIsLogical();
		/* Start the receiver thread */
		receiverThread = new Thread(this, "Receiver thread");
		receiverThread.start();
	}
	
	private void logNewEvent(TimeStampedMessage msg) {
		String src;
		ArrayList<LogMessage> eventList = null;
		LogMessage newLoggedMsg = new LogMessage(((LogMessage)(msg.getData())));
		src = msg.getSrc();
		
		eventList = eventMap.get(src);
		if (eventList == null) {
			eventList = new ArrayList<LogMessage>();
			eventMap.put(src, eventList);
		}
		
		listAddEvent(eventList, newLoggedMsg);
		/*
		if (checkDup(eventList, newLoggedMsg) == false) {
			eventList.add(newLoggedMsg);
		} else {
			System.out.println("Received a dup event: " + 
								newLoggedMsg.toString() + " from: " + src);
		}
		*/
	}
	
	private boolean listAddEvent(ArrayList<LogMessage> eventList, LogMessage newEvent)
	{
		boolean isSuccess = true;
		int index = 0;
		
		if (eventList.isEmpty()) {
			/* Empty list why check anything */
			eventList.add(newEvent);
			return isSuccess;
		}
		
		for (LogMessage loggedEvent : eventList) {

			if ((loggedEvent.getEventTS().compare(newEvent.getEventTS()) == TimeStampRelation.equal)) {
				System.out.println("[LOGGER]: Received duped event: " + newEvent.toString());
				isSuccess = false;
				return isSuccess;
			} else if ((loggedEvent.getEventTS().compare(newEvent.getEventTS()) == TimeStampRelation.lessEqual)) {
				index++;
			}
		}
		
		eventList.add(index, newEvent);
		
		return isSuccess;
	}
	
	/*
	private boolean checkDup(ArrayList<LogMessage> eventList, LogMessage newEvent) 
	{
		boolean isDup = false;
		
		for (LogMessage loggedEvent : eventList) {
			
			if ((loggedEvent.getEventTS().compare(newEvent.getEventTS()) == TimeStampRelation.equal)) {
				isDup = true;
				break;
			}
		}
		
		return isDup;
	}*/

	private void dumpEventMaps() {
		
		ArrayList<LogMessage> loggedMsgs;
		
		System.out.println("[LOGGER]: Dump Events Start");
		
		for (Entry<String, ArrayList<LogMessage>> mapEntry: eventMap.entrySet()) {
			
			System.out.println("\t =============== key: " + mapEntry.getKey() + " =======================");
			loggedMsgs = mapEntry.getValue();
			
			for (LogMessage loggedMsg : loggedMsgs) {
				//loggedMsg.dumpLoggedMsg();
				System.out.println(loggedMsg.toString());
			}
			
			System.out.println("\t ======================================");
		}
		
		System.out.println("[LOGGER]: Dump Events End");
	}
	
	/*
	private void generateSequence()
	{
		ArrayList<LogMessage> loggedMsgs;
		LogMessage firstLoggedMsg;

		for (Entry<String, ArrayList<LogMessage>> mapEntry: eventMap.entrySet()) {
			System.out.println("\t [LOGGER]: GenSeq key: " + mapEntry.getKey());
			loggedMsgs = mapEntry.getValue();
			
			firstLoggedMsg = loggedMsgs.get(0);
			System.out.println("[LOGGER]: getSequence EVENT: " + firstLoggedMsg.getTSMsg().getMsgTS().toString());
			getSequence(firstLoggedMsg);
		}
	}
	*/
	
	private void generateSequence()
	{
		ArrayList<LogMessage> loggedMsgs;
		LogMessage firstLoggedMsg;

		for (Entry<String, ArrayList<LogMessage>> mapEntry: eventMap.entrySet()) {
//System.out.println("\t [LOGGER]: GenSeq key: " + mapEntry.getKey());
			loggedMsgs = mapEntry.getValue();

			firstLoggedMsg = loggedMsgs.get(0);
			appendSequenceNextEvent(startMsgs, firstLoggedMsg);
//System.out.println("[LOGGER]: Genseq key: " + mapEntry.getKey() + " DONE");
		}
		
		/* We have got first level messages now lets get the sequence */
		for (LogMessage msgIter: startMsgs) {
			firstLoggedMsg = msgIter;
			getSequence(firstLoggedMsg);
		}
	}
		
	/*
	private void displaySequence()
	{
		ArrayList<LogMessage> loggedMsgs;
		LogMessage firstLoggedMsg;

		for (Entry<String, ArrayList<LogMessage>> mapEntry: eventMap.entrySet()) {
			
			System.out.println("\t Disp Seq key: " + mapEntry.getKey());
			
			loggedMsgs = mapEntry.getValue();

			firstLoggedMsg = loggedMsgs.get(0);
			displaySequence(firstLoggedMsg);
		}
	}
	*/
	private void displaySequence()
	{
		LogMessage firstLoggedMsg;
		
		System.out.println("==============================");
		for (LogMessage msgIter: startMsgs) {
			firstLoggedMsg = msgIter;
			displaySequence(firstLoggedMsg);
		}
	}
	
	/*
	private void displayLogicalSequence()
	{
		ArrayList<LogMessage> loggedMsgs;

		for (Entry<String, ArrayList<LogMessage>> mapEntry: eventMap.entrySet()) {
			loggedMsgs = mapEntry.getValue();
			System.out.println("\n==============================\n");
			for (LogMessage loggedMsg : loggedMsgs) {
				System.out.print(loggedMsg.getTSMsg().getMsgTS().toString() + "==>");
			}
		}
	}
	*/
	private void displayLogicalSequence()
	{
		ArrayList<LogMessage> loggedMsgs;

		for (Entry<String, ArrayList<LogMessage>> mapEntry: eventMap.entrySet()) {
			loggedMsgs = mapEntry.getValue();
			System.out.println("\n==============================\n");
			Iterator<LogMessage> it = loggedMsgs.iterator();

			while (it.hasNext()) {
				LogMessage loggedMsg = it.next();
				System.out.print(loggedMsg.toString());
				
				if (it.hasNext()) {
					System.out.print(" => ");
				} else {
					System.out.print("\n");
				}
			}	 
		}
	}
	
	private void getSequence(LogMessage startEvent)
	{
		LogMessage nextMsg = null;
		
		/*
		 *  Get next level sequence form all the hash buckets.
		 */
		for (Entry<String, ArrayList<LogMessage>> mapEntry: eventMap.entrySet()) {
			
			nextMsg = getSequenceNextEvent(mapEntry, startEvent);
			if (nextMsg != null) {
				appendSequenceNextEvent(startEvent.getNextMsgs(), nextMsg);
			}
		}

		/* 
		 * Lets iterate through all the next level events to get there successors
		 */
		for (LogMessage loggedMsg: startEvent.getNextMsgs()) {
			getSequence(loggedMsg);
		}
	}

	/*
	private void displaySequence(LogMessage startEvent)
	{

		System.out.println(startEvent.toString());

		for (LogMessage loggedMsg: startEvent.getNextMsgs()) {
			displaySequence(loggedMsg);
			System.out.println("===========================================");
		}
	}
	 */
	
	private void displaySequence(LogMessage startEvent)
	{
		Iterator<LogMessage> it = startEvent.getNextMsgs().iterator();
		LogMessage loggedMsg;
		
		System.out.println(startEvent.toString());
		
		while (it.hasNext()) {
			loggedMsg = it.next();
			System.out.print(" => ");
			displaySequence(loggedMsg);
			System.out.println("===========================================");
			if (it.hasNext()) {
				/* We are changing the path in the sequence print parent node again */
				System.out.print(startEvent.toString());
			}
		}
	}
	
	private void cleanupSequence()
	{
		ArrayList<LogMessage> loggedMsgs;

		for (Entry<String, ArrayList<LogMessage>> mapEntry: eventMap.entrySet()) {
			loggedMsgs = mapEntry.getValue();

			for (LogMessage loggedMsg : loggedMsgs) {
				loggedMsg.nextMsgs.clear();
			}
		}
		
		startMsgs.clear();
	}
	
	private void cleanupAll()
	{
		ArrayList<LogMessage> loggedMsgs;

		for (Entry<String, ArrayList<LogMessage>> mapEntry: eventMap.entrySet()) {
			loggedMsgs = mapEntry.getValue();

			for (LogMessage loggedMsg : loggedMsgs) {
				loggedMsg.nextMsgs.clear();
			}
			
			loggedMsgs.clear();
		}
		
		eventMap.clear();
		startMsgs.clear();
	}
	
	private LogMessage getSequenceNextEvent(Entry<String, ArrayList<LogMessage>> mapEntry, LogMessage curEvent)
	{
		LogMessage retMsg = null;
		ArrayList<LogMessage> loggedMsgs;
		TimeStamp loggedTS = null;
		TimeStamp curTS = curEvent.getEventTS();
		
		loggedMsgs = mapEntry.getValue();
		for (LogMessage loggedMsg : loggedMsgs) {
			
			loggedTS = loggedMsg.getEventTS();
			
			if (curTS.compare(loggedTS) == TimeStampRelation.lessEqual) {
				retMsg = loggedMsg;
				break;
			}
		}
		
		return retMsg;
	}
	
	/*
	private void appendSequenceNextEvent(ArrayList<LogMessage> nextMsgs, LogMessage nextMsg)
	{	
		TimeStamp loggedTS = null;
		TimeStamp nextTS = nextMsg.getTSMsg().getMsgTS();
		LogMessage removeMessage = null;
		
		for (LogMessage loggedMsg : nextMsgs) {
			
			loggedTS = loggedMsg.getTSMsg().getMsgTS();
			if (nextTS.compare(loggedTS) == TimeStampRelation.lessEqual) {
				removeMessage = loggedMsg;
				break;
			}
		}
		
		if (removeMessage != null) {
			nextMsgs.remove(removeMessage);
		}
		
		System.out.println("[LOGGER] APPEND msg: " + nextMsg.getTSMsg().getMsgTS().toString());
		nextMsgs.add(nextMsg);
		return;
	}
	*/
	
	private void appendSequenceNextEvent(ArrayList<LogMessage> nextMsgs, LogMessage nextMsg)
	{	
		TimeStamp loggedTS = null;
		TimeStamp nextTS = nextMsg.getEventTS();
		LogMessage removeMessage = null;
		boolean isConcurrent = true;
		
		for (LogMessage loggedMsg : nextMsgs) {
			
			loggedTS = loggedMsg.getEventTS();
			if (nextTS.compare(loggedTS) == TimeStampRelation.lessEqual) {
				removeMessage = loggedMsg;
				break;
			}
		}
		
		if (removeMessage != null) {
//System.out.println("[LOGGER] REMOVE msg: " + removeMessage.getTSMsg().getMsgTS().toString());
			nextMsgs.remove(removeMessage);
		}
		
		/* 
		 * We'll add this message only if it is not concurrent with
		 * existing messages. 
		 */
		for (LogMessage loggedMsg : nextMsgs) {

			loggedTS = loggedMsg.getEventTS();
			if (nextTS.compare(loggedTS) != TimeStampRelation.concurrent) {
				isConcurrent = false;
				break;
			}
		}
		
		if (isConcurrent == true) {
//System.out.println("[LOGGER] APPEND msg: " + nextMsg.getTSMsg().getMsgTS().toString());
			nextMsgs.add(nextMsg);
		} else {
//System.out.println("[LOGGER] NO APPEND msg: " + nextMsg.getTSMsg().getMsgTS().toString());
		}
		
		return;
	}
	
	public void run() 
	{
		TimeStampedMessage msg = null;
		
		System.out.println("[LOGGER]: Started the new thread");
		
		while (true) {
			msg = (TimeStampedMessage)(this.msgPasser.receiveLogger());
    		if(msg != null) {
//System.out.println("[LOGGER]: Received a new message");
//msg.dumpMsg();
    			this.logNewEvent(msg);
    		}
		}
	}
	
	public void executing() {
		String cmdInput = new String();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        Message msg = null;
        
        while (!cmdInput.equals("quit")) {
            System.out.print("CommandLine% ");
            try {
                cmdInput = in.readLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            if(cmdInput.equals("quit")) {
            	
            	try {
					this.msgPasser.closeAllSockets();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	System.exit(0);
            	
            } else if(cmdInput.equals("ps")) {
            	
            	this.dumpEventMaps();
            	
            } else if (cmdInput.equals("sequence")) {

            	if (this.isLogical == false) {
            		this.generateSequence();
            		this.displaySequence();
            		this.cleanupSequence();
            	} else {
            		this.displayLogicalSequence();
            	}
            	
            } else if (cmdInput.equals("concurrent")) {

            	if (this.isLogical == false) {
            		this.printVectorConcurrent();
            	} else {
            		this.printLogicalConcurrent();
            	}
            	
            } else if (cmdInput.equals("cleanup")) {
//System.out.println("we clean up the logger");
            	this.msgPasser.cleanUp();
            	this.cleanupAll();

            } else if (!cmdInput.equals(null) && !cmdInput.equals("\n")) {
            	
            	String[] array = cmdInput.split(" ");
            	if(array.length == 3)
            		this.msgPasser.send(new TimeStampedMessage(array[0], array[1], array[2], null, this.msgPasser.getLocalName()));
            	else if(cmdInput.equals("receive")) {
            		msg = this.msgPasser.receiveLogger();
            		if(msg == null) {
            			System.out.println("Nothing to pass to Aplication!");
            		}
            		else {
            			System.out.println("We receive");
           				System.out.println(msg.toString());
            		}
            	}
            	else {
            		System.out.println("Invalid Command!");
            	}
            	
            } else {
            	
            	System.out.println("Invalid Command!");
            }
        }
	}
	
	public void printVectorConcurrent() {
		for(String e : this.eventMap.keySet()) {
			for(LogMessage f : this.eventMap.get(e)) {
				TimeStamp ts = f.getEventTS();
				for(String str : this.eventMap.keySet()) {
					for(LogMessage lm : this.eventMap.get(str)) {
						if(ts.compare(lm.getEventTS()) == TimeStampRelation.concurrent) {
							System.out.println(lm.getEventTS().toString() + " || " + f.getEventTS().toString());
						}
					}
				}
				System.out.println("====================================");
			}
		}
	}
	
	public void printLogicalConcurrent() {
		for(String e : this.eventMap.keySet()) {
			for(LogMessage f : this.eventMap.get(e)) {
				//System.out.println("\n" + f.msg.getMsgTS().toString() + " " + "concurrent messages : ");
				for(String str : this.eventMap.keySet()) {
					for(LogMessage lm : this.eventMap.get(str)) {
						if(e.equals(str) == false) {
							System.out.println(e + ":" + f.getEventTS().toString() + " || " + 
						str + ":" + lm.getEventTS().toString());
						}
					}
				}
				System.out.println("====================================");
			}
		}
	}
	
	public static void main(String[] args) {
		if(args.length != 2) {
			System.out.println("Arguments mismtach.\n" +
					"Required arguments - <Yaml config> <name of host>");
			System.exit(0);
		}
		System.out.println("MAIN Logger");
		MessagePasser msgPasser = new MessagePasser(args[0], args[1]);
		/* Start the receiver thread */
		Logger logger = new Logger(msgPasser);
		logger.executing();
	}
}