package org.or1k.vm.exceptions;

public class DPFException extends OR1KException {
	
	private int addr;
	
	public DPFException(int addr) {
		this.addr = addr;
	}

	public int getAddr() {
		return addr;
	}

}
