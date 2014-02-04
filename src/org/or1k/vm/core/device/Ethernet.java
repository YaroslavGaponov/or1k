package org.or1k.vm.core.device;

import org.or1k.vm.core.Device;
import org.or1k.vm.core.IDevice32;

public class Ethernet extends Device implements IDevice32 {

	@Override
	public void reset() {

	}

	@Override
	public void write32(int addr, int value) {
	}

	@Override
	public int read32(int addr) {
		return -1;
	}

}
