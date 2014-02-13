/* 18-842 Distributed Systems
 * Lab 2
 * Group 30 - aboos & dil1
 */

import java.util.ArrayList;
import java.util.List;


public class Group {

	private String groupName;

	private List<String> memberList;
	private List<Member> members;

	public Group() {
		this.memberList = new ArrayList<String>();
		this.members = new ArrayList<Member>();
	}
	public Group(String name) {
		this.groupName = name;
		this.memberList = new ArrayList<String>();
		this.members = new ArrayList<Member>();
	}
	
	public Group(String groupName, ArrayList<String> memberList) {
		this.groupName = groupName;
		this.memberList = memberList;
	}
	
	public void addMember(String newUser) {
		this.memberList.add(newUser);
	}
	
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public List<String> getMemberList() {
		return memberList;
	}

	public void setMemberList(List<String> memberList) {
		this.memberList = memberList;
	}
	
	public List<Member> getMembers() {
		return members;
	}
	public void setMembers(List<Member> members) {
		this.members = members;
	}

	@Override
	public String toString() {
		return "Group [groupName=" + groupName + ", memberList=" + memberList
				+ ", members=" + members + "]";
	}
	
}
