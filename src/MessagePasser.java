/* 18-842 Distributed Systems
 * Lab 2
 * Group 30 - aboos & dil1
 */

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class MessagePasser {

	private final int NACK_CHECK_MS = 15000;

	// store the delayed send msg
	private LinkedList<Message> delaySendQueue = new LinkedList<Message>();
	// store the delayed recv msg
	private LinkedList<Message> delayRecvQueue = new LinkedList<Message>();
	// store all the received msg from all receive sockets
	private LinkedList<Message> recvQueue = new LinkedList<Message>();
	private HashMap<String, ObjectOutputStream> outputStreamMap = 
		new HashMap<String, ObjectOutputStream>();
	private Map<SocketInfo, Socket> sockets = new HashMap<SocketInfo, Socket>();

	private Map<SrcGroup, List<Message>> holdBackMap = 
		new HashMap<SrcGroup, List<Message>>();
	private Map<NackItem, Message> allMsg = new HashMap<NackItem, Message>();

	private String configFilename;

	private String localName;
	private ServerSocket hostListenSocket;
	private SocketInfo hostSocketInfo;


	private Config config;
	private static int currSeqNum;

	private ClockService clockSer;
	// map of SEEN seqNums
	private Map<SrcGroup, Integer> seqNums = new HashMap<SrcGroup, Integer>();
	private Map<String, Integer> sendSeqNums = new HashMap<String, Integer>();

	
	//map of from hostname to G(hostname)
	private Map<String, String> rscGroupMap = new HashMap<String, String>();
	

  private enum RuleType {
		SEND, RECEIVE,
	}

	/**
	 * Listens on designated port for incoming messages
	 */
	public class startListen extends Thread {

		public startListen() {

		}

		public void run() {
			System.out.println("Running");
			try {
				while (true) {
					Socket sock = hostListenSocket.accept();
					new ListenThread(sock).start();
				}
			} catch (IOException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	/**
	 * Sends a periodic NACK of the next item in each relevant group
	 * Catches when last missed message was the last
	 */

	public class sendPeriodNACK extends Thread {
		public sendPeriodNACK() {
		}

		@SuppressWarnings("static-access")
		public void run() {
			while (true) {
				sendNACK();
				try {
					Thread.currentThread().sleep(NACK_CHECK_MS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * When connection made, starts receiving on Socket
	 */

	public class ListenThread extends Thread {
		private Socket LisSock = null;

		public ListenThread(Socket sock) {
			this.LisSock = sock;
		}

		public void run() {

			try {
				ObjectInputStream in = new ObjectInputStream(
						this.LisSock.getInputStream());

				while (true) {
					TimeStampedMessage msg = (TimeStampedMessage) in
							.readObject();

					if (msg.getKind().equals("NACK")) {
						getNACK(msg);
					} else {
						parseConfig();
						Rule rule = null;
						if ((rule = matchRule(msg, RuleType.RECEIVE)) != null) {
							if (rule.getAction().equals("drop")) {
								synchronized (delayRecvQueue) {
									while (!delayRecvQueue.isEmpty()) {
										checkAdd(delayRecvQueue.pollLast());
									}
								}
								continue;
							} else if (rule.getAction().equals("duplicate")) {
								System.out.println("Duplicating message");
								synchronized (recvQueue) {
									checkAdd(msg);
									checkAdd(msg.makeCopy());

									synchronized (delayRecvQueue) {
										while (!delayRecvQueue.isEmpty()) {
											checkAdd(delayRecvQueue.pollLast());
										}
									}
								}
							} else if (rule.getAction().equals("delay")) {
								synchronized (delayRecvQueue) {
									delayRecvQueue.add(msg);
								}
							} else {
								System.out.println("We receive a wierd msg!");
							}
						} else {
							synchronized (recvQueue) {
								checkAdd(msg);
								synchronized (delayRecvQueue) {
									while (!delayRecvQueue.isEmpty()) {
										checkAdd(delayRecvQueue.pollLast());
									}
								}
							}
						}

					}
				}
			} catch (EOFException e2) {
				System.out.println("A peer disconnected");
				for (Map.Entry<SocketInfo, Socket> entry : sockets.entrySet()) {
					if (this.LisSock.getRemoteSocketAddress().equals(
							entry.getValue().getLocalSocketAddress())) {
						System.out.println("Lost connection to "
								+ entry.getKey().getName());
						try {
							ObjectOutputStream out = outputStreamMap.get(entry
									.getKey().getName());
							outputStreamMap.remove(entry.getKey().getName());
							out.close();

							sockets.remove(entry.getKey());
							entry.getValue().close();
						} catch (IOException e) {
							// Auto-generated catch block
							e.printStackTrace();
						}
						break;
					}
				}

			} catch (IOException e1) {
				// Auto-generated catch block
				e1.printStackTrace();
			} catch (ClassNotFoundException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	
	/**
	 * Constructor for class. Starts everything running.
	 */

	public MessagePasser(String configuration_filename, String local_name) {
		configFilename = configuration_filename;
		localName = local_name;
		currSeqNum = 1;
		try {
			parseConfig();
		} catch (FileNotFoundException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}

		/* debug for parsing yaml to see whether we get the correct groups */
		for (Group g : this.config.getGroupList()) {
			System.out.println(g.toString());
		}
		/* end debug */
		/*
		 * Now, using localName get *this* MessagePasser's SocketInfo and setup
		 * the listening socket and all other sockets to other hosts.
		 * 
		 * We can optionally, save this info in hostSocket and hostSocketInfo to
		 * avoid multiple lookups into the 'sockets' Map.
		 */

		/* for clockService */
		if (this.config.isLogical == true) {
			this.clockSer = new LogicalClockService(new TimeStamp());
		} else {
			this.clockSer = new VectorClockService(new TimeStamp());
		}

		if (!this.config.isLogical) {
			HashMap<String, Integer> map = this.clockSer.getTs()
					.getVectorClock();
			for (SocketInfo e : this.config.configuration) {
				map.put(e.getName(), 0);
			}
		}

		/* */
		hostSocketInfo = config.getConfigSockInfo(localName);
		if (hostSocketInfo == null) {
			/*** ERROR ***/
			System.out.println("The local name is not correct.");
			System.exit(0);
		} else {
			/* Set up socket */
			System.out.println("For this host: " + hostSocketInfo.toString());
			try {
				hostListenSocket = new ServerSocket(hostSocketInfo.getPort(),
						10, InetAddress.getByName(hostSocketInfo.getIp()));
			} catch (IOException e) {
				/*** ERROR ***/
				System.out.println("Cannot start listen on socket. "
						+ e.toString());
				System.exit(0);
			}
			/* need to initiate the seqNums map */
			for (Group g : this.config.getGroupList()) {
				SrcGroup tmp = new SrcGroup(this.localName, g.getGroupName());
				this.seqNums.put(tmp, 0);
				if (g.getMemberList().contains(this.localName)) {
					for (SocketInfo s : this.config.configuration) {
						SrcGroup tmpArray = new SrcGroup(s.getName(),
								g.getGroupName());
						this.seqNums.put(tmpArray, 0);
					}
				}
			}

			/* start the listen thread */
			new startListen().start();
			new sendPeriodNACK().start();

		}
	}
	
	/**
	 * Send function called by application. 
	 * Sets timestamp and calls function to continue sending
	 * @param message: message to send
	 */

	public void send(Message message) {
		
		/* update the timestamp */
		this.clockSer.addTS(this.localName);
		((TimeStampedMessage) message).setMsgTS(this.clockSer.getTs()
				.makeCopy());
		System.out.println("TS add by 1");
		message.set_source(localName);
		checkSend(message);

	}
	
	/**
	 * Checks if message standard or multicast. Configures multicast.
	 * @param message: message to send
	 */

	private void checkSend(Message message) {
		// check if multicast
		if (config.getGroup(message.getDest()) != null) { // multicast message
			Group sendGroup = config.getGroup(message.getDest());
			SrcGroup srcGrp = new SrcGroup(localName, sendGroup.getGroupName());
			int sNum = updateSendSequenceNumber(sendGroup.getGroupName());
			// change to update function
			NackItem ni = new NackItem(srcGrp, sNum);
			((TimeStampedMessage) message).setGrpSeqNum(sNum);
			String grouName = new String(message.getDest());
			message.setGrpDest(grouName);
			allMsg.put(ni, message);

			for (String member : sendGroup.getMemberList()) {
				message.setDest(member);
				applyRulesSend(message, member);
			}

		} else { // regular message
			applyRulesSend(message, message.getDest());
		}
	}
	
	/**
	 * Applies send rules to individual messages
	 * @param message: message to send
	 * @param dest: destination of message
	 */

	private void applyRulesSend(Message message, String dest) {
		try {
			parseConfig();
		} catch (FileNotFoundException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		message.set_source(localName);
		message.set_seqNum(currSeqNum++);

		Rule rule = null;
		if ((rule = matchRule(message, RuleType.SEND)) != null) {
			if (rule.getAction().equals("drop")) {
				return;
			} else if (rule.getAction().equals("duplicate")) {
				Message dupMsg = message.makeCopy();
				dupMsg.set_duplicate(true);

				/* Send 'message' and 'dupMsg' */
				doSend(message, dest);
				((TimeStampedMessage) dupMsg).setMsgTS(this.clockSer.getTs()
						.makeCopy());
				doSend(dupMsg, dest);

				/*
				 * We need to send delayed messages after new message. This was
				 * clarified in Live session by Professor.
				 */
				for (Message m : delaySendQueue) {
					doSend(m,m.getDest());
				}
				delaySendQueue.clear();

			} else if (rule.getAction().equals("delay")) {
				delaySendQueue.add(message);
			} else {
				System.out.println("We get a weird message here!");
			}
		} else {
			doSend(message, dest);

			/*
			 * We need to send delayed messages after new message. This was
			 * clarified in Live session by Professor.
			 */
			
			if(!(message.getKind().equalsIgnoreCase("NACK") || 
					message.getKind().equalsIgnoreCase("NACK_REPLY"))){
				for (Message m : delaySendQueue) {
					doSend(m, m.getDest());
				}
				delaySendQueue.clear();
			}
		}
	}
	
	
	/**
	 * Send message over socket
	 * @param message: message to send
	 * @param dest: destination of message
	 */

	private void doSend(Message message, String dest) {

		TimeStampedMessage msg = (TimeStampedMessage) message;

		/* end fill */
		Socket sendSock = null;
		for (SocketInfo inf : sockets.keySet()) {
			if (inf.getName().equals(dest)) {
				sendSock = sockets.get(inf);
				break;
			}
		}
		if (sendSock == null) {
			try {
				SocketInfo inf = config.getConfigSockInfo(dest);
				if (inf == null) {
					System.out.println("Cannot find config for " + dest);
					return;
				}
				sendSock = new Socket(inf.getIp(), inf.getPort());
			} catch (ConnectException e2) {
				return;
			} catch (IOException e) {
				// Auto-generated catch block
				e.printStackTrace();
				return;
			}
			sockets.put(config.getConfigSockInfo(dest), sendSock);
			try {
				outputStreamMap.put(dest,
						new ObjectOutputStream(sendSock.getOutputStream()));
			} catch (IOException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
		}

		ObjectOutputStream out;
		try {
			out = outputStreamMap.get(dest);
			out.writeObject(msg);
			out.flush();

		} catch (SocketException e1) {
			// System.out.println("Peer " + dest + " is offline. Cannot send");

		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Receive function called by application
	 * @return: message: message at head of receive queue, or null if empty
	 */

	public Message receive() {
		/*
		 * Re-parse the config. Receive the message using sockets. Finally,
		 * check message against receiveRules.
		 */

		synchronized (recvQueue) {
			if (!recvQueue.isEmpty()) {
				Message popMsg = recvQueue.remove();
				/* add ClockService */
				TimeStampedMessage msg = (TimeStampedMessage) popMsg;
				// System.out.println("new Debug sentence");
				// msg.dumpMsg();
				this.clockSer.updateTS(msg.getMsgTS());
				this.clockSer.addTS(this.localName);
				/* */

				return popMsg;
			}
		}

		return null;
	}
	
	
	/**
	 * Receives a logger message
	 * @return message: log message received
	 */

	public Message receiveLogger() {
		/*
		 * Re-parse the config. Receive the message using sockets. Finally,
		 * check message against receiveRules.
		 */

		synchronized (recvQueue) {
			if (!recvQueue.isEmpty()) {
				Message popMsg = recvQueue.remove();

				return popMsg;
			}
		}

		return null;
	}
	
	/**
	 * Matches a message to a list of rules
	 * @param message: message to check
	 * @param RuleType: type of rules
	 */

	public Rule matchRule(Message message, RuleType type) {
		List<Rule> rules = null;

		if (type == RuleType.SEND) {
			rules = config.getSendRules();
		} else {
			rules = config.getReceiveRules();
		}

		if (rules == null) {
			return null;
		}

		for (Rule r : rules) {
			if (!r.getSrc().isEmpty()) {
				if (!message.getSrc().equals(r.getSrc())) {
					continue;
				}
			}

			if (!r.getDest().isEmpty()) {
				if (!message.getDest().equals(r.getDest())) {
					continue;
				}
			}

			if (!r.getKind().isEmpty()) {
				if (!message.getKind().equals(r.getKind())) {
					continue;
				}

			}

			if (r.getSeqNum() != -1) {
				if (message.getSeqNum() != r.getSeqNum()) {
					continue;
				}
			}

			if (!r.getDuplicate().isEmpty()) {
				if (!(message.isDuplicate() == true
						&& r.getDuplicate().equals("true") || message
						.isDuplicate() == false
						&& r.getDuplicate().equals("false"))) {
					continue;
				}
			}

			return r;
		}
		return null;
	}
	

	/**
	 * Check if the message is multicast. If multicast check for correct 
	 * sequence number order
	 * @param msg: message received
	 */

	public void checkAdd(Message msg) {

		if (msg.getKind().equals("NACK REPLY")) {
			msg = (TimeStampedMessage) msg.getData();
		}
		if (msg.getGrpDest() != null) { // multicast message
			SrcGroup srcGrp = new SrcGroup(msg.getSrc(), msg.getGrpDest());
			int getNum = ((TimeStampedMessage) msg).getGrpSeqNum();
			NackItem ni = new NackItem(srcGrp, getNum);
			allMsg.put(ni, msg);

			int seenNum = seqNums.get(srcGrp);

			if (seenNum >= getNum) { // duplicate message. Ignore
				return;
			} else if ((seenNum + 1) == getNum) { // in order. add to recvQueue
				updateSequenceNumber(srcGrp);
				addToRecvQueue(msg);
				updateHoldback(msg);
			} else {
				addToHoldBack(msg);
				sendNACK();
			}
		} else { // regular message
			addToRecvQueue(msg);
		}
	}
	
	
	/**
	 * Adds message to correct holdback queue in order
	 * @param msg: message received
	 */

	public void addToHoldBack(Message msg) {
		SrcGroup srcGrp = new SrcGroup(msg.getSrc(), msg.getGrpDest());
		List<Message> messagesInGroup;
		if (holdBackMap.containsKey(srcGrp)) {
			messagesInGroup = holdBackMap.get(srcGrp);
			// insert in order
			int i;
			for (i = 0; i < messagesInGroup.size(); i++) {
				if (((TimeStampedMessage) msg).getGrpSeqNum() < 
						((TimeStampedMessage) messagesInGroup.get(i))
						.getGrpSeqNum()) {
					messagesInGroup.add(i, msg);
					break;
				}
			}

			if (i == messagesInGroup.size()) {
				messagesInGroup.add(msg);
			}
		} else {
			messagesInGroup = new ArrayList<Message>();
			messagesInGroup.add(msg);
		}

		holdBackMap.put(srcGrp, messagesInGroup);

		return;
	}
	
	
	/**
	 * Adds to receive queue in order
	 * @param msg: message received
	 */
	
	public void addToRecvQueue(Message msg) {
		int i = 0, size = 0;
		synchronized (recvQueue) {
			size = recvQueue.size();
			for (; i < size; i++) {
				TimeStampedMessage tmp = (TimeStampedMessage) recvQueue.get(i);
				if (((TimeStampedMessage) msg).getMsgTS().compare(
						tmp.getMsgTS()) != TimeStampRelation.greaterEqual) {
					break;
				}
			}
			recvQueue.add(i, msg);
		}
	}
	
	/**
	 * Updates the received sequence number for a unique pair of source/group
	 * @param srcGrp: array of [source, group]
	 */


	public void updateSequenceNumber(SrcGroup srcGrp) {
		int curr;
		if (seqNums.containsKey(srcGrp)) {
			curr = seqNums.get(srcGrp);
		} else {
			curr = 0;
		}
		seqNums.put(srcGrp, curr + 1);
	}
	
	
	/**
	 * Updates the sequence number for the current user to send to a group
	 * @param group: name of group
	 */

	public int updateSendSequenceNumber(String group) {
		int curr;
		if (sendSeqNums.containsKey(group)) {
			curr = sendSeqNums.get(group);
		} else {
			curr = 0;
		}
		curr++;
		sendSeqNums.put(group, curr);
		return curr;
	}

	public void updateHoldback(Message msg) {
		SrcGroup srcGrp = new SrcGroup(msg.getSrc(), msg.getGrpDest());
		if (holdBackMap.containsKey(srcGrp)) {
			List<Message> messagesInGroup = holdBackMap.get(srcGrp);
			while (!messagesInGroup.isEmpty()
					&& (int) ((TimeStampedMessage) messagesInGroup.get(0))
							.getGrpSeqNum() == (int) (seqNums.get(srcGrp) + 1)){
				updateSequenceNumber(srcGrp);
				addToRecvQueue(messagesInGroup.get(0));
				messagesInGroup.remove(0);
			}
		}
	}
	
	
	/**
	 * Sends NACKs for missing messages
	 */

	public void sendNACK() {

		for (Group g : config.getGroupList()) {
			if (g.getMemberList().contains(localName)) {
				List<NackItem> nackContent = new ArrayList<NackItem>();
				for (SocketInfo e : config.configuration) {
					List<NackItem> nackContentSrc = new ArrayList<NackItem>();
					SrcGroup srcGrp = new SrcGroup(e.getName(),
							g.getGroupName());

					if (holdBackMap.get(srcGrp) != null
							&& !holdBackMap.get(srcGrp).isEmpty()) {
						// something in holdback queue
						List<Message> hbQueue = holdBackMap.get(srcGrp);
						int seqNum = seqNums.get(srcGrp) + 1;
						Iterator<Message> queueIt = hbQueue.iterator();
						// Go through queue and populate missing msg NACKs
						while (queueIt.hasNext()) {
							Message curr = queueIt.next();
							while (seqNum < ((TimeStampedMessage) curr)
									.getGrpSeqNum()) {
								NackItem nack = new NackItem(srcGrp, seqNum);
								nackContent.add(nack);
								nackContentSrc.add(nack);
								seqNum++;
							}
							seqNum++;
						}
					} else { // nothing in holdback queue, just NACK next
						NackItem nack = new NackItem(srcGrp,
								seqNums.get(srcGrp) + 1);
						nackContent.add(nack);
						nackContentSrc.add(nack);
					}
					// send to original sender (might not be in group)
					TimeStampedMessage nackMsgSrc = new TimeStampedMessage(
							srcGrp.getSrc(), "NACK", nackContentSrc, null,
							this.localName);
					nackMsgSrc.set_source(localName);
					doSend(nackMsgSrc, srcGrp.getSrc());
				}
				TimeStampedMessage nackMsg = new TimeStampedMessage(
						g.getGroupName(), "NACK", nackContent, null,
						this.localName);
				nackMsg.set_source(localName);
				for (String member : g.getMemberList()) {
					nackMsg.setGrpDest(nackMsg.getDest());
					nackMsg.setDest(member);
					applyRulesSend(nackMsg, member);
				}
			}
		}
	}

	public void getNACK(Message msg) {
		@SuppressWarnings("unchecked")
		List<NackItem> nackContent = (List<NackItem>) msg.getData();

		for (NackItem nack : nackContent) {
			if (allMsg.containsKey(nack)) { // if has message
				Message nackReply = new TimeStampedMessage(msg.getSrc(),
						"NACK REPLY", allMsg.get(nack), null, this.localName);
				doSend(nackReply, nackReply.getDest());
			}
		}
	}
	
	/**
	 * Parses out the configuration file into the config object
	 */

	private void parseConfig() throws FileNotFoundException {
		InputStream input = new FileInputStream(new File(configFilename));
		Constructor constructor = new Constructor(Config.class);
		SocketInfo mySocketInfo;
		Yaml yaml = new Yaml(constructor);

		/* SnakeYAML will parse and populate the Config object for us */
		config = (Config) yaml.load(input);
		/* some tricky hack to take care of Groups */
		for (Group g : this.config.getGroupList()) {
			List<String> tmpList = g.getMemberList();
			tmpList.clear();
			for (Member e : g.getMembers()) {
				tmpList.add(e.getMembername());
			}
		}
		/*
		 * XXX: Assigning config.isLogical based on SocketInfo data is a big
		 * hack. I could not make it work with normal yaml.load, hence had to go
		 * with this hack.
		 */
		mySocketInfo = config.getConfigSockInfo(localName);
		if (mySocketInfo == null) {
			/*** ERROR ***/
			System.out.println("The local name is not correct.");
			System.exit(0);
		}

		if (mySocketInfo.getClockType().equals("logical")) {
			config.isLogical = true;
		} else {
			config.isLogical = false;
		}

	}
	
	
	/**
	 * Closes all the sockets
	 */

	public void closeAllSockets() throws IOException {
		// Auto-generated method stub
		hostListenSocket.close();

		/* Close all other sockets in the sockets map */
		for (Map.Entry<SocketInfo, Socket> entry : sockets.entrySet()) {
			entry.getValue().close();
		}
		for (Map.Entry<String, ObjectOutputStream> entry : outputStreamMap
				.entrySet()) {
			entry.getValue().close();
		}
	}
	

	
	/**
	 * Clears out queues and resets clock
	 */

	public void cleanUp() {
		this.delayRecvQueue.clear();
		this.delayRecvQueue.clear();
		this.recvQueue.clear();
		this.clockSer.cleanUp();

		if (!this.config.isLogical) {
			HashMap<String, Integer> map = this.clockSer.getTs()
					.getVectorClock();
			for (SocketInfo e : this.config.configuration) {
				map.put(e.getName(), 0);
			}
		}
	}

	@Override
	public String toString() {
		return "MessagePasser [configFilename=" + configFilename
				+ ", localName=" + localName + ", hostListenSocket="
				+ hostListenSocket + ", hostSocketInfo=" + hostSocketInfo
				+ ", config=" + config + "]";
	}

	public boolean getIsLogical() {
		return this.config.isLogical;
	}
	
	
	/**
	 * Setters and getters for variables
	 */

	public ClockService getClockSer() {
		return clockSer;
	}

	public void setClockSer(ClockService clockSer) {
		this.clockSer = clockSer;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}
	

	public Map<SrcGroup, List<Message>> getHoldBackMap() {
		return holdBackMap;
	}

	public void setHoldBackMap(Map<SrcGroup, List<Message>> holdBackMap) {
		this.holdBackMap = holdBackMap;
	}

	public Map<NackItem, Message> getAllMsg() {
		return allMsg;
	}

	public void setAllMsg(Map<NackItem, Message> allMsg) {
		this.allMsg = allMsg;
	}

	public Map<SrcGroup, Integer> getSeqNums() {
		return seqNums;
	}

	public void setSeqNums(Map<SrcGroup, Integer> seqNums) {
		this.seqNums = seqNums;
	}

	public Map<String, String> getRscGroupMap() {
	   return rscGroupMap;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}
  public void setRscGroupMap(Map<String, String> rscGroupMap) {
    this.rscGroupMap = rscGroupMap;
  }

}