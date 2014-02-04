package org.or1k.vm.core;

import java.util.ArrayList;
import java.util.List;

import org.or1k.vm.core.Device;

class Range {
	private final int start;
	private final int stop;
	
	public Range(int start, int stop) {
		this.start = start;
		this.stop = stop;
	}
	
	public boolean inRange(int addr) {
		return (addr >= start) && (addr <= stop);
	}
	
	public int getDelta(int addr) {
		return addr - start;
	}
}

public class RAM {
	
	private final byte[] data;
	
	private List<Device> devices = new ArrayList<Device>();	
	private List<Range> ranges = new ArrayList<Range>();
	
	public RAM(int size) {
		data = new byte[size];
	}
	
	public void addDevice(int addr, int range, Device device) {
		devices.add(device);
		ranges.add(new Range(addr, addr + range));
	}

	public void write8(int idx, byte val) {
		data[idx] = val;
	}	
	
	public void write8u(int idx, int val) {
		if (idx >= 0) {
			data[idx ^ 3] = (byte) (val & 0xff);
		} else {
			for(int i=0; i<ranges.size(); i++) {
				if (ranges.get(i).inRange(idx)) {
					IDevice8 device = (IDevice8) devices.get(i);
					device.write8(ranges.get(i).getDelta(idx), val);
					return;
				}
			}
			System.out.println(String.format("Device 0x%08x is not found!!!", idx)); System.exit(0);
		}		
	}
	
	public void write8s(int idx, int val) {
		if (idx >= 0) {
			data[idx ^ 3] = (byte) val;
		} else {
			for(int i=0; i<ranges.size(); i++) {
				if (ranges.get(i).inRange(idx)) {
					IDevice8 device = (IDevice8) devices.get(i);
					device.write8(ranges.get(i).getDelta(idx), val);
					return;
				}
			}
			System.out.println(String.format("Device 0x%08x is not found!!!", idx)); System.exit(0);
		}		
	}
	
	public int read8u(int idx) {
		if (idx >= 0) {
			return data[idx ^ 3] & 0xff;
		} else {
			for(int i=0; i<ranges.size(); i++) {
				if (ranges.get(i).inRange(idx)) {
					IDevice8 device = (IDevice8) devices.get(i);
					return device.read8(ranges.get(i).getDelta(idx));					
				}
			}						
		}
		
		System.out.println(String.format("Device 0x%08x is not found!!!", idx)); System.exit(0);
		
		return 0;
	}
	
	public int read8s(int idx) {
		if (idx >= 0) {
			return data[idx ^ 3];
		} else {
			for(int i=0; i<ranges.size(); i++) {
				if (ranges.get(i).inRange(idx)) {
					IDevice8 device = (IDevice8) devices.get(i);
					return device.read8(ranges.get(i).getDelta(idx));					
				}
			}			
		}
		
		System.out.println(String.format("Device 0x%08x is not found!!!", idx)); System.exit(0);
		
		return 0;
	}
	
	public void write16u(int idx, int val) {
		if (idx >= 0) {
			write8u(idx + 1, val);
			write8u(idx + 0, val >>> 8);
		} else {
			for(int i=0; i<ranges.size(); i++) {
				if (ranges.get(i).inRange(idx)) {
					IDevice16 device = (IDevice16) devices.get(i);
					device.write16(ranges.get(i).getDelta(idx), val);
					return;
				}
			}
			System.out.println(String.format("Device 0x%08x is not found!!!", idx)); System.exit(0);
		}		
	}
	public void write16s(int idx, int val) {
		if (idx >= 0) {
			short s = (short) val;
			write8u(idx + 1, s);
			write8u(idx + 0, s >>> 8);
		} else {
			for(int i=0; i<ranges.size(); i++) {
				if (ranges.get(i).inRange(idx)) {
					IDevice16 device = (IDevice16) devices.get(i);
					device.write16(ranges.get(i).getDelta(idx), val);
					return;
				}
			}
			System.out.println(String.format("Device 0x%08x is not found!!!", idx)); System.exit(0);
		}		
	}
	
	public int read16u(int idx) {
		if (idx >= 0) {
			return (read8u(idx + 0) << 8) | read8u(idx + 1);
		} else {
			for(int i=0; i<ranges.size(); i++) {
				if (ranges.get(i).inRange(idx)) {
					IDevice16 device = (IDevice16) devices.get(i);
					return device.read16(ranges.get(i).getDelta(idx));					
				}
			}						
		}
		System.out.println(String.format("Device 0x%08x is not found!!!", idx)); System.exit(0); return 0;
	}
	
	public int read16s(int idx) {
		if (idx >= 0) {
			return (short) ((read8u(idx + 0) << 8) | read8u(idx + 1));
		} else {
			for(int i=0; i<ranges.size(); i++) {
				if (ranges.get(i).inRange(idx)) {
					IDevice16 device = (IDevice16) devices.get(i);
					return device.read16(ranges.get(i).getDelta(idx));					
				}
			}						
		}
		System.out.println(String.format("Device 0x%08x is not found!!!", idx)); System.exit(0); return 0;
	}
	
	public void write32(int idx, int val) {		
		if (idx >= 0) {
			write16u(idx + 2, val);
			write16u(idx + 0, val >>> 16);
		} else {
			for(int i=0; i<ranges.size(); i++) {
				if (ranges.get(i).inRange(idx)) {
					IDevice32 device = (IDevice32) devices.get(i);
					device.write32(ranges.get(i).getDelta(idx), val);
					return;
				}
			}
			System.out.println(String.format("Device 0x%08x is not found!!!", idx)); System.exit(0);
		}
	}
	
	public int read32(int idx) {	
		if (idx >= 0) {
			return (read16u(idx + 0) << 16) | read16u(idx + 2);
		} else {
			for(int i=0; i<ranges.size(); i++) {
				if (ranges.get(i).inRange(idx)) {
					IDevice32 device = (IDevice32) devices.get(i);
					return device.read32(ranges.get(i).getDelta(idx));					
				}
			}						
		}
		System.out.println(String.format("Device 0x%08x is not found!!!", idx)); System.exit(0); return 0;
	}
}
