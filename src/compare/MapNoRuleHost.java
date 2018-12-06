package compare;


public class MapNoRuleHost {

	private String hostName;
	private Double ram;
	private Double disk;
	private Integer vCPUs;
	private Boolean physical;
	
	public MapNoRuleHost(String hostName) {
		this.hostName = hostName;
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

	public Integer getvCPUs() {
		return vCPUs;
	}

	public void setvCPUs(Integer vCPUs) {
		this.vCPUs = vCPUs;
	}

	public Boolean getPhysical() {
		return physical;
	}

	public void setPhysical(int physical) {
		if(physical==0) {
			this.physical = false;
		}else {
			this.physical = true;
		}
	}
	
	public void decrease(Double disk, Double ram, Integer cpu) {
		System.out.println("host name is " + this.hostName);
		System.out.println("before decrease");
		System.out.println("disk is " + this.disk);
		System.out.println("ram is " + this.ram);
		System.out.println("cpu is " + this.vCPUs);
		this.decreaseCPUs(cpu);
		this.decreaseDisk(disk);
		this.decreaseRam(ram);
		System.out.println("after decrease");
		System.out.println("disk is " + this.disk);
		System.out.println("ram is " + this.ram);
		System.out.println("cpu is " + this.vCPUs);
	}
	
	public Double decreaseDisk(Double disk) {
		this.disk -= disk;
		return this.disk;
	}
	
	public Double decreaseRam(Double ram) {
		this.ram -= ram;
		return this.ram;
	}
	
	public int decreaseCPUs(int cpu) {
		this.vCPUs -= cpu;
		return this.vCPUs;
	}
	
	public boolean check(Double disk, Double ram, Integer cpu, Integer physical) {
		if(physical==1 && !this.physical) {
			return false;
		}
		if(this.vCPUs < cpu) {
			System.out.println(this.hostName + " !!cpu不够");
			return false;
		}
		
		if(this.disk < disk) {
			System.out.println(this.hostName + "disk不够");
			return false;
		}
		
		if(this.ram < ram) {
			System.out.println(this.hostName + "ram 不够");
			return false;
		}
		return true;
	}
	
	
}
