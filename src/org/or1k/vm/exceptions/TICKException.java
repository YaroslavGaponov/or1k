package org.or1k.vm.exceptions;

public class TICKException extends OR1KException {
	private int addr;
	
	public TICKException(int addr) {
		this.addr = addr;
	}
	
	public int getAddr() {
		return addr;
	}

}
