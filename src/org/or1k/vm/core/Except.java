package org.or1k.vm.core;

public enum Except {
	
	ITLBMISS(0xA00),
	IPF(0x400),
	RESET(0x100),
	DTLBMISS(0x900),
	DPF(0x300),
	BUSERR(0x200),
	TICK(0x500),
	INT(0x800),
	SYSCALL(0xc00);
	
	private int addr;
	
	private Except(int addr) {
		this.addr = addr;
	}
	
	public int getAddr() {
		return addr;
	}

}
