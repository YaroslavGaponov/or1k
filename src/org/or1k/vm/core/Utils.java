package org.or1k.vm.core;

class Utils {
	
	public static long uint(int i) {
		return Long.parseLong(Integer.toBinaryString(i), 2);
	}
	
	public static boolean getBit(int val, int n) {
		return ((val >>> n) & 1) == 1;
	}
}
