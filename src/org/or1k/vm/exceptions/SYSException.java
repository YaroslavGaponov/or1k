package org.or1k.vm.exceptions;

public class SYSException extends OR1KException {
	
	private int addr;
	
	public SYSException(int addr) {
		this.addr = addr;
	}

	public int getAddr() {
		return addr;
	}

}
