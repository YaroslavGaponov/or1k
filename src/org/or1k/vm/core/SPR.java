package org.or1k.vm.core;

public class SPR {
	
	public static final int VR = 0;	
	public static final int UPR = 1;	
	public static final int DMMUCFGR = 3;
	public static final int IMMUCFGR = 4;	
	public static final int DCCFGR = 5;
	public static final int ICCFGR = 6;
	public static final int DCFGR = 7;
	public static final int PCCFGR = 8;
	public static final int VR2 = 9;
	public static final int AVR = 10;
	public static final int EVBAR = 11;
	public static final int AECR = 12;
	public static final int AESR = 13;
	public static final int NPC = 16;
	public static final int SR = 17;	
	public static final int PPC = 18;
	public static final int FPCSR = 20;
	public static final int EPCR_BASE = 32;
	public static final int EEAR_BASE = 48;	
	public static final int ESR_BASE = 64;
	public static final int PICMR = (9 << 11) | 0;
	public static final int PICSR = (9 << 11) | 2;
	
	private int[][] spr = new int[32][1024];
	
	public SPR() {
		
	}
	
	public void set(int rgroup, int rindex, int value){		
		spr[rgroup][rindex] = value;
	}
	
	public void set(int idx, int value) {
		int rgroup = (idx >>> 11) & 0x1f;
		int rindex = idx & 0x7ff;
		if (rgroup == 9 && rindex == 0) value |= 3;
		set(rgroup, rindex, value);
	}
	
	public int get(int rgroup, int rindex) {
		return spr[rgroup][rindex];
	}
	
	public int get(int idx) {
		int rgroup = (idx >>> 11) & 0x1f;
		int rindex = idx & 0x7ff;
		return get(rgroup, rindex);
	}
		
}
