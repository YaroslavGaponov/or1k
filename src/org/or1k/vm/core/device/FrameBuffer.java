package org.or1k.vm.core.device;

import org.or1k.vm.core.Device;
import org.or1k.vm.core.IDevice32;

public class FrameBuffer extends Device implements IDevice32 {

	private int address;
	private int width;
	private int height;
	
	public FrameBuffer() {
		reset();
	}
	
	@Override
	public void write32(int addr, int value) {
		switch(addr) {
			case 0x14:
				address = ((value & 0xff) << 24) | ((value & 0xff00) << 8) | ((value >>> 8) & 0xff00) | ((value >>> 24) & 0xff);
				break;
		}
	}

	@Override
	public int read32(int addr) {
		return 0;
	}

	@Override
	public void reset() {
		address = 16000000;
		width = 640;
		height = 400;		
	}

}
