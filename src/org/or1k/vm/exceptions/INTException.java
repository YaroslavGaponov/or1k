package org.or1k.vm.exceptions;

public class INTException extends OR1KException {
	private int addr;
	
	public INTException(int addr) {
		this.addr = addr;
	}
	
	public int getAddr() {
		return addr;
	}
}
