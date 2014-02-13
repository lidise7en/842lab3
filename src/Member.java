/* 18-842 Distributed Systems
 * Lab 2
 * Group 30 - aboos & dil1
 */

public class Member {

	private String membername;
	
	public Member() {
		membername = "";
	}
	public Member(String membername) {
		this.membername = membername;
	}
	

	public String getMembername() {
		return membername;
	}
	public void setMembername(String membername) {
		this.membername = membername;
	}
	
	@Override
	public String toString() {
		return "Member [membername=" + membername + "]";
	}
	


}
