package org.or1k.vm.core;

public enum FLAG {
	SM(0), 
	TEE(1), 
	IEE(2), 
	DCE(3), 
	ICE(4), 
	DME(5), 
	IME(6), 
	LEE(7), 
	CE(8), 
	F(9), 
	CY(10), 
	OV(11), 
	OVE(12), 
	DSX(13), 
	EPH(14), 
	FO(15), 
	SUMRA(16);
	
	int bit;
	FLAG(int bit) {
		this.bit = bit;
	}
	
	public int get() {
		return 1 << bit;
	}
}
