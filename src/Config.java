/**
 * @file Config.java
 * @brief Object to hold the paramters parsed from the configuration file
 * @author  aboos
 * 			dil1
 * @date 02/09/2014
 */

import java.util.List;

public class Config {

	List<SocketInfo> configuration;
	List<Rule> sendRules;
	List<Rule> receiveRules;
	List<Group> groupList;
	boolean isLogical;

	public Config() {
	}
	
	public List<SocketInfo> getConfiguration() {
		return configuration;
	}
	public void setConfiguration(List<SocketInfo> hosts) {
		this.configuration = hosts;
	}
	public List<Rule> getSendRules() {
		return sendRules;
	}
	public void setSendRules(List<Rule> sendRules) {
		this.sendRules = sendRules;
	}
	public List<Rule> getReceiveRules() {
		return receiveRules;
	}
	public void setReceiveRules(List<Rule> receiveRules) {
		this.receiveRules = receiveRules;
	}
	
	public SocketInfo getConfigSockInfo(String name) {
		for(SocketInfo s : configuration) {
			if(s.getName().equals(name)) {
				return s;
			}
		}
		return null;
	}
	
	public Group getGroup(String name) {
		for(Group g : groupList) {
			if(g.getGroupName().equals(name)) {
				return g;
			}
		}
		return null;
	}
	
	public List<Group> getGroupList() {
		return groupList;
	}

	public void setGroupList(List<Group> groupList) {
		this.groupList = groupList;
	}

	public List<String> findGroupMember(String groupName) {
		for(Group e : this.groupList) {
			if(e.getGroupName().equals(groupName))
				return e.getMemberList();
		}
		System.out.println("We cannot find this group");
		return null;
	}

	@Override
	public String toString() {
		return "Config [configuration=" + configuration + ", sendRules="
				+ sendRules + ", receiveRules=" + receiveRules + ", groupList="
				+ groupList + ", isLogical=" + isLogical + "]";
	}	

}
