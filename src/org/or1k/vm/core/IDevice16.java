package org.or1k.vm.core;

public interface IDevice16  {
	void write16(int addr, int value);
	int read16(int addr);
}
