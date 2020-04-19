package nachos.vm;

public class PageItem {
	int pid;
	int vpn;
	
	public PageItem(int pid, int vpn) {
		this.pid = pid;
		this.vpn = vpn;
	}
	
	public int getPID() {
		return pid;
	}
	
	public int getVPN() {
		return vpn;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof PageItem)
			return ((PageItem) o).pid == pid && ((PageItem) o).vpn == vpn;
		return false;
	}

	@Override
	public int hashCode() {
		return pid ^ vpn;
	}

}
