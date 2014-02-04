package org.or1k.vm.core;

public interface IDevice32  {
	void write32(int addr, int value);
	int read32(int addr);
}
