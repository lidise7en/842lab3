import java.util.HashSet;
import java.util.LinkedList;

public class ResourceClient {

  private MessagePasser mp;
  private ClientState state;
  private boolean voted;
  private LinkedList<Message> recvPermitQueue = new LinkedList<Message>();
  private LinkedList<Message> recvWaitQueue = new LinkedList<Message>();
  private Integer numMsgSent = 0;
  private Integer numMsgReceived = 0;


private String voteHost;
  private static boolean DEBUG = true;
  
  public enum ClientState {
    RELEASED, WANTED, HELD
  }

  public ResourceClient(MessagePasser msgPasser) {
    this.mp = msgPasser;
    this.state = ClientState.RELEASED;
    this.voted = false;
    this.voteHost = null;
    new listenThread().start();
  }
  
  @Override
  public String toString() {
    return "ResourceClient [mp=" + mp + ", state=" + state + ", voted=" + voted
        + "]";
  }
  
  public void getResource() {
	  if(this.state == ClientState.RELEASED) {
		  this.state = ClientState.WANTED;
		  String groupName = this.mp.getRscGroupMap().get(this.mp.getLocalName());
		  synchronized(this.recvPermitQueue) {
			  this.recvPermitQueue.clear();
		  }
		  sendRequests(groupName, "RESOURCE_REQ");
		  
		  (new collectResponse()).start();
	  }
	  else {
		  System.out.println("Not in initial state");
	  }
  }
  
  public void releaseResource() {
    if(this.state == ClientState.HELD) {
      this.state = ClientState.RELEASED;
      String groupName = this.mp.getRscGroupMap().get(this.mp.getLocalName());
      sendRequests(groupName, "RESOURCE_RELEASE");
    }
  }
  
  public void sendRequests(String groupList, String kind) {
	  if(groupList == null) {
		  System.out.println("The group is illegal");
		  return;
	  }
	  this.mp.send(new TimeStampedMessage(groupList, kind, "", this.mp.getClockSer().getTs(), this.mp.getLocalName()));
	  synchronized(numMsgSent) {
	    //TODO: get group size, shouldn't hard code here
	    numMsgSent += 3;
	  }
  }
  
  public void addToQueue(LinkedList<Message> queue, TimeStampedMessage msg) {
    int i = 0, size = 0;
    synchronized (queue) {
      size = queue.size();
      for (; i < size; i++) {
        TimeStampedMessage tmp = (TimeStampedMessage) queue.get(i);
        if ((msg).getMsgTS().compare(
            tmp.getMsgTS()) != TimeStampRelation.greaterEqual) {
          break;
        }
      }
      queue.add(i, msg);
    }
    if (DEBUG) {
      String queuename = queue.equals(recvPermitQueue) ? "Permit Queue" : "Wait Queue";
      System.out.println("Adding msg in queue" + queuename + ": " + msg.toString());
    }
  }
  
  public MessagePasser getMp() {
    return mp;
  }
  
  public void setMp(MessagePasser mp) {
    this.mp = mp;
  }
  
  public ClientState getState() {
    return state;
  }
  
  public void setState(ClientState state) {
    this.state = state;
  }
  
  public boolean isVoted() {
    return voted;
  }
  
  public void setVoted(boolean voted) {
    this.voted = voted;
  }
  
  public String getVoteHost() {
	return voteHost;
  }

  public void setVoteHost(String voteHost) {
	this.voteHost = voteHost;
  }

  public void printStatus() {
    if (state == ClientState.HELD)
      System.out.printf("Client " + mp.getLocalName() + ": currently granted resource!\n");
    else 
      System.out.printf("Client " + mp.getLocalName() + ": currently being blocked!\n");
    System.out.println("Message sent: " + numMsgSent + ", received: " + numMsgReceived);
    System.out.println("voteHost is" + this.voteHost);
  }
  
  public class listenThread extends Thread {
    public listenThread() {}
    
    public void run() {
      while(true) {
        TimeStampedMessage receiveMsg = (TimeStampedMessage)mp.receive();
        if (receiveMsg != null) {
          if (DEBUG)
            System.out.println("Received: " + receiveMsg.toString());
          
          synchronized(numMsgReceived) {
            numMsgReceived++;
          }
          
          if(receiveMsg.getKind().equals("RESOURCE_REQ")) {
            if(state == ClientState.HELD || voted) {
              if(!receiveMsg.getSrc().equals(voteHost)) {
            	synchronized(recvWaitQueue) {
            		addToQueue(recvWaitQueue, receiveMsg); 
            	}
              }
            }
            else {
              String dest = receiveMsg.getSrc();
              TimeStampedMessage msg = new TimeStampedMessage(dest, "RESOURCE_RESPONSE", "", mp.getClockSer().getTs(), mp.getLocalName());
              mp.send(msg);
              if (DEBUG)
                System.out.println("Sent: " + msg.toString());
              
              synchronized(numMsgSent) {
                numMsgSent++;
              }
              voted = true;
              voteHost = dest;
            }
          }
          else if(receiveMsg.getKind().equals("RESOURCE_RELEASE")) {
            if(!recvWaitQueue.isEmpty() && receiveMsg.getSrc().equals(voteHost)) {
              synchronized(recvWaitQueue) {
                TimeStampedMessage removedMsg = (TimeStampedMessage)recvWaitQueue.remove();
                String dest = removedMsg.getSrc();
                TimeStampedMessage msg = new TimeStampedMessage(dest, "RESOURCE_RESPONSE", "", mp.getClockSer().getTs(), mp.getLocalName());
                mp.send(msg);
                if (DEBUG)
                  System.out.println("Sent: " + msg.toString());
                
                synchronized(numMsgSent) {
                  numMsgSent++;
                }
                voted = true;
                voteHost = dest;
              }
            }
            else {
              voted = false;
              voteHost = null;
            }
          }
          else if(receiveMsg.getKind().equals("RESOURCE_RESPONSE")) {
            synchronized(recvPermitQueue) {
              addToQueue(recvPermitQueue, receiveMsg);
            }
          }
          else {
            System.out.println("Wierd Message received in Client");
          }
        }
        
        try {
          Thread.sleep(1000L);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }
  
  public class collectResponse extends Thread {

    public collectResponse() {
      
    }

    public void run() {
      HashSet<String> memberSet = new HashSet<String>();
      for(Group g : mp.getConfig().groupList) {
        
        if(g.getGroupName().equals(mp.getRscGroupMap().get(mp.getLocalName()))) {
          for(String str : g.getMemberList()) {
            memberSet.add(str);
          }
          break;
        }
      }
      while(memberSet.size() != 0) {
        synchronized(recvPermitQueue) {
          if(!recvPermitQueue.isEmpty()) {
            TimeStampedMessage receiveMsg = (TimeStampedMessage)recvPermitQueue.remove();
            if(receiveMsg != null && receiveMsg.getKind().equals("RESOURCE_RESPONSE")) {
              String src = receiveMsg.getSrc();
              if(memberSet.contains(src)) {
                memberSet.remove(src);
              }
            }
          }
        }
        
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      state = ClientState.HELD;
    }
  }
}
