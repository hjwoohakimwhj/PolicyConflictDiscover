package gate;

public class Host {
	private String hostName;
	private Integer vCPUs;
	private Double ram;
	private Double disk;
	private boolean physical;
	
	
	public Host(String hostName, Integer vCPUs, Double ram, Double disk, boolean physical) {
		setDisk(disk);
		setHostName(hostName);
		setRam(ram);
		setvCPUs(vCPUs);
		this.physical = physical;
	}
	
	/*
	 * 默认返回剩余CPU数目
	 * 返回-1代表资源不够
	 * 用大于等于0来判断合不合适
	 */
	public Integer consumeCPU(Integer num) {
		if(num>vCPUs) {
			return -1;
		}
		vCPUs -= num;
		return vCPUs;
		
	}

	public Double consumeRAM(Double num) {
		if(num>ram) {
			return (double)-1;
		}
		ram -= num;
		return ram;
	}

	public Double consumeDisk(Double num) {
		if(num>disk) {
			return (double)-1;
		}
		disk -= num;
		return disk;
	}
	
	private boolean checkvCPUs(Integer vCPUs) {
		if((this.vCPUs - vCPUs) >= 0) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean checkRAM(Double num) {
		if((this.ram - num) >= 0) {
			return true;
		}else {
			return false;
		}
	}
	private boolean checkDisk(Double num) {
		if((this.disk - num) >= 0) {
			return true;
		}else {
			return false;
		}
	}
	
	
	public boolean checkAvail(Integer cpuN, Double diskN, Double ramN, Integer physical) {
		if(physical==1) {
			if(!this.physical) {
				return false;
			}
		}
		if(!this.checkvCPUs(cpuN)) {
			System.out.println("host name is " + this.hostName + " vCPUs is " 
					+ this.vCPUs + "require is " + cpuN);
			return false;
		}
		if(!this.checkDisk(diskN)){
			System.out.println("host name is " + this.hostName + " disk is " 
					+ this.disk + "require is " + diskN);
			return false;
		}
		if(!this.checkRAM(ramN)){
			System.out.println("host name is " + this.hostName + " ram is " 
					+ this.ram + "require is " + ramN);
			return false;
		}
		return true;
	}


	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public Integer getvCPUs() {
		return vCPUs;
	}

	public void setvCPUs(Integer vCPUs) {
		this.vCPUs = vCPUs;
	}

	public Double getRam() {
		return ram;
	}

	public void setRam(Double ram) {
		this.ram = ram;
	}

	public Double getDisk() {
		return disk;
	}

	public void setDisk(Double disk) {
		this.disk = disk;
	}
}
