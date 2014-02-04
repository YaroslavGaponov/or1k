package org.or1k.vm.core;

public class SR {
		
	private int flags;
	
	public SR() {		
	}
	
	public SR(int flags) {
		this.flags = flags;
	}
	
	public boolean get(FLAG flag) {
		return (((flags >>> flag.bit) & 1) == 1);
	}

	public void set(FLAG flag, boolean value) {
		if (value) {
			flags |= 1 << flag.bit;
		} else {
			flags &= ~(1 << flag.bit);
		}		
	}	
	
	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}
}
