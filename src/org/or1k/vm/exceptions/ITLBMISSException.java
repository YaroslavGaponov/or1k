package org.or1k.vm.exceptions;

public class ITLBMISSException extends OR1KException {
	
	private int addr;
	
	public ITLBMISSException(int addr) {
		this.addr = addr;
	}
	
	public int getAddr() {
		return addr;
	}
	

}
