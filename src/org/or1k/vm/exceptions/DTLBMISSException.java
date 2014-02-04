package org.or1k.vm.exceptions;

public class DTLBMISSException extends OR1KException {

	private int addr;
	
	public DTLBMISSException(int addr) {
		this.addr = addr;
	}
	
	public int getAddr() {
		return addr;
	}

}
