/**
 * @file CmdTool.java
 * @brief Interactive application with user
 * @author  aboos
 * 			dil1
 * @date 02/09/2014
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CmdTool {
	/*
	 * Command Type:
	 * quit: quit the whole process
	 * ps: print the information of current MessagePasser
	 * send command: dest <kind> <data>
	 * send log command : log <dest> <kind> <data>
	 * receive command: receive
	 * receive log command : receive log
	 * 
	 */
	private MessagePasser msgPasser;
	private ResourceClient rscClient;
	
	public CmdTool(MessagePasser msgPasser, ResourceClient rscClient) {
		this.msgPasser = msgPasser;
		this.rscClient = rscClient;
	}
	public void executing() {
		String cmdInput = new String();
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    Message msg = null;
    LogMessage logMsg = null;
        
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
      	
      	System.out.println(this.msgPasser.toString());
      	
      } 
      else if(cmdInput.equals("cleanup")) {
      	System.out.println("we clean up the messagePasser and ClockService");
      	this.msgPasser.cleanUp();
      }
      else if(cmdInput.equals("printAllMsg")) {
      	System.out.println("The Map of allMsg is");
      	for(NackItem n : this.msgPasser.getAllMsg().keySet()) {
      		System.out.println(n.toString() + " " + this.msgPasser.getAllMsg().get(n).toString());
      	}
      }
      else if(cmdInput.equals("printHoldBackMap")) {
      	System.out.println("The Map of HoldBack Queue is");
      	for(SrcGroup s : this.msgPasser.getHoldBackMap().keySet()) {
      		System.out.println(s.toString() + " " + this.msgPasser.getHoldBackMap().get(s).toString());
      	}
      }
      else if(cmdInput.equals("printSeqNums")) {
      	System.out.println("The Map of SeqNums");
      	for(SrcGroup s : this.msgPasser.getSeqNums().keySet()) {
      		System.out.println(s.toString() + " " + this.msgPasser.getSeqNums().get(s).toString());
      	}
      }
      else if (!cmdInput.equals(null) && !cmdInput.equals("\n")) {
            	
      	String[] array = cmdInput.split(" ");
      	if(array.length == 3)
      		this.msgPasser.send(new TimeStampedMessage(array[0], array[1], array[2], null, this.msgPasser.getLocalName()));
      	
      	else if(cmdInput.equals("receive")) {
      		msg = this.msgPasser.receive();		
      		if(msg == null) {
      			System.out.println("Nothing to pass to Aplication!");
      		} else {
      			System.out.println("We receive");
      			((TimeStampedMessage)msg).dumpMsg();
      		}
      	} 
      	else if(array.length == 2) {
      		if(array[0].equals("receive") && array[1].equals("log")) {
      			msg = this.msgPasser.receive();
      			if(msg == null) {
      				System.out.println("Nothing to pass to Aplication!");
      			} else {
      				logMsg = new LogMessage(((TimeStampedMessage)msg).getMsg(), this.msgPasser.getClockSer().getTs().makeCopy());
      				TimeStampedMessage newLogMsg = new TimeStampedMessage("logger", "log", logMsg, null, this.msgPasser.getLocalName());
      				this.msgPasser.send(newLogMsg);
      				System.out.println("We receive");
      				((TimeStampedMessage)msg).dumpMsg();
       				//this.msgPasser.logEvent(((TimeStampedMessage)msg).getMsg(), this.msgPasser.getClockSer().getTs().makeCopy());
      			}
      		}
      		else if(array[0].equals("event")) {
      		  System.out.println("Lamport time " + this.msgPasser.getClockSer().getTs().getLamportClock());
      		  this.msgPasser.getClockSer().addTS(this.msgPasser.getLocalName());
      		  System.out.println("Lamport time " + this.msgPasser.getClockSer().getTs().getLamportClock());
      			logMsg = new LogMessage(array[1], this.msgPasser.getClockSer().getTs().makeCopy());
      			TimeStampedMessage newLogMsg = new TimeStampedMessage("logger", "log", logMsg, null, this.msgPasser.getLocalName());
      			this.msgPasser.send(newLogMsg);
      			//this.msgPasser.logEvent(array[1], this.msgPasser.getClockSer().getTs().makeCopy());
      		}
      		else if (array[0].equals("resource")) {
      		  if (array[1].equals("get")) {
      		    rscClient.getResource();
      		  }
      		  else if (array[1].equals("release")) {
      		    rscClient.releaseResource();
      		  }
      		  else {
      		    System.out.println("Invalid Command!");
      		  }
      		}
      		else {
      			System.out.println("Invalid Command!");
      		}
      	}
      	else if(array.length == 4) {
      		if(array[0].equals("log")) {
      			TimeStampedMessage newMsg = new TimeStampedMessage(array[1], array[2], array[3], null, this.msgPasser.getLocalName());
      			this.msgPasser.send(newMsg);
      			System.out.println("send TS:" + this.msgPasser.getClockSer().getTs());
			
      			logMsg = new LogMessage(newMsg.getMsg(), newMsg.getMsgTS().makeCopy());
      			TimeStampedMessage newLogMsg = new TimeStampedMessage("logger", "log", logMsg, null, this.msgPasser.getLocalName());
      			this.msgPasser.send(newLogMsg);
      			//this.msgPasser.logEvent(newMsg.getMsg(), this.msgPasser.getClockSer().getTs().makeCopy());
      		}
      		else {
      			System.out.println("Invalid Command!");
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
	

	public static void main(String[] args) {
		if(args.length != 2) {
			System.out.println("Arguments mismtach.\n" +
					"Required arguments - <Yaml config> <name of host>");
			System.exit(0);
		}
		MessagePasser msgPasser = new MessagePasser(args[0], args[1]);
		ResourceClient rscClient = new ResourceClient(msgPasser);
		CmdTool tool = new CmdTool(msgPasser, rscClient);
		tool.executing();
	}
}
