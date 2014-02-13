import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;



public class ResourceClient {

	public enum ClientState {
	    RELEASED, WANTED, HELD
	}
  private MessagePasser mp;

  private ClientState state;
  private boolean voted;
  private LinkedList<Message> recvPermitQueue = new LinkedList<Message>();
  private LinkedList<Message> recvWaitQueue = new LinkedList<Message>();
  
  public class listenThread extends Thread {
	  public listenThread() {}
	  public void run() {
		  while(true) {
			  TimeStampedMessage receiveMsg = (TimeStampedMessage)mp.receive();
			  if(receiveMsg.getKind().equals("RESOURCE_REQ")) {
				  if(state == ClientState.HELD || voted) {
					  recvWaitQueue.add(receiveMsg);
				  }
				  else {
					  String dest = receiveMsg.getSrc();
					  mp.send(new TimeStampedMessage(dest, "RESOURCE_RESPONSE", "", mp.getClockSer().getTs(), mp.getLocalName()));
					  voted = true;
				  }
			  }
			  else if(receiveMsg.getKind().equals("RESOURCE_RELEASE")) {
				  if(!recvWaitQueue.isEmpty()) {
					  TimeStampedMessage removedMsg = (TimeStampedMessage)recvWaitQueue.remove();
					  String dest = removedMsg.getSrc();
					  mp.send(new TimeStampedMessage(dest, "RESOURCE_RESPONSE", "", mp.getClockSer().getTs(), mp.getLocalName()));
					  voted = true;
				  }
				  else {
					  voted = false;
				  }
			  }
			  else if(receiveMsg.getKind().equals("RESOURCE_RESPONSE")) {
				  synchronized(recvPermitQueue) {
					  recvPermitQueue.add(receiveMsg);
				  }
			  }
			  else {
				  System.out.println("Wierd Message received in Client");
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			state = ClientState.HELD;
			
		}
	}
  public ResourceClient(MessagePasser msgPasser) {
    this.mp = msgPasser;
    this.state = ClientState.RELEASED;
    this.voted = false;
    new listenThread().start();
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
  
  @Override
  public String toString() {
    return "ResourceClient [mp=" + mp + ", state=" + state + ", voted=" + voted
        + "]";
  }
  
  public void getResource() {
	  if(this.state == ClientState.RELEASED && !this.voted) {
		  this.state = ClientState.WANTED;
		  String groupName = this.mp.getRscGroupMap().get(this.mp.getLocalName());
		  synchronized(this.recvPermitQueue) {
			  this.recvPermitQueue.clear();
		  }
		  sendGetMessages(groupName);
		  
		  (new collectResponse()).start();
	  }
	  else {
		  System.out.println("Not in initial state");
	  }
  }
  
  public void sendGetMessages(String groupList) {
	  if(groupList == null) {
		  System.out.println("The group is illegal");
		  return;
	  }
	  this.mp.send(new TimeStampedMessage(groupList, "RESOURCE_REQ", "", this.mp.getClockSer().getTs(), this.mp.getLocalName()));
	  
  }
}
