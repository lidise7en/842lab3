
public class ResourceClient {


  private MessagePasser mp;
  private enum State {
    RELEASED, WANTED, HELD
  }
  private State state;
  private boolean voted;
  
  public ResourceClient(MessagePasser msgPasser) {
    this.mp = msgPasser;
    this.state = State.RELEASED;
    this.voted = false;
  }
  
  public MessagePasser getMp() {
    return mp;
  }
  public void setMp(MessagePasser mp) {
    this.mp = mp;
  }
  public State getState() {
    return state;
  }
  public void setState(State state) {
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
}
