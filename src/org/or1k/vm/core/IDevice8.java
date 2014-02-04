package org.or1k.vm.core;

public interface IDevice8 {
	void write8(int addr, int value);
	int read8(int addr);
}
