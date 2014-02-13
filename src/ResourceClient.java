import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;



public class ResourceClient {

	public enum ClientState {
	    RELEASED, WANTED, HELD
	}
  private MessagePasser mp;

  private ClientState state;
  private boolean voted;
  
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
				
				TimeStampedMessage receiveMsg = (TimeStampedMessage)mp.receive();
				if(receiveMsg != null && receiveMsg.getKind().equals("RESOURCERESPONSE")) {
					String src = receiveMsg.getSrc();
					if(memberSet.contains(src)) {
						memberSet.remove(src);
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
	  this.state = ClientState.WANTED;
	  String groupName = this.mp.getRscGroupMap().get(this.mp.getLocalName());
	  sendGetMessages(groupName);
	  (new collectResponse()).start();
  }
  
  public void sendGetMessages(String groupList) {
	  if(groupList == null) {
		  System.out.println("The group is illegal");
		  return;
	  }
	  this.mp.send(new TimeStampedMessage(groupList, "RESOURCEREQ", "", this.mp.getClockSer().getTs(), this.mp.getLocalName()));
	  
  }
}
