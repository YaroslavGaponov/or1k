package org.or1k.vm.exceptions;


public class IPFException extends OR1KException {
	
	private int addr;
	
	public IPFException(int addr) {
		this.addr = addr;
	}
	
	public int getAddr() {
		return addr;
	}

}
