/* 18-842 Distributed Systems
 * Lab 2
 * Group 30 - aboos & dil1
 */

public class SocketInfo {

	private String name;
	private String ip;
	int port;
	private String clockType = "logical"; // Workaround
	
	public SocketInfo() {
		
	}
	


	public SocketInfo(String name, String ip, int port) {
		this.name = name;
		this.ip = ip;
		this.port = port;
	}
	
	public SocketInfo(String name, String ip, int port, String clockType) {
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.clockType = clockType;
	}
	
	public String getClockType() {
		return this.clockType;
	}
	
	public String getName() {
		return name;
	}

	public void setClockType(String clockType) {
		this.clockType = clockType;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((clockType == null) ? 0 : clockType.hashCode());
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SocketInfo other = (SocketInfo) obj;
		if (clockType == null) {
			if (other.clockType != null)
				return false;
		} else if (!clockType.equals(other.clockType))
			return false;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (port != other.port)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "SocketInfo [name=" + name + ", ip=" + ip + ", port=" + port
				+ ", clockType=" + clockType + "]";
	}
}
