package org.or1k.vm.asm;

import java.util.ArrayList;
import java.util.List;

public class Program {
	
	public enum REG { 
		r0,	 r1,  r2,  r3,  r4,  r5,  r6,  r7,  
		r8,  r9,  r10, r11, r12, r13, r14, r15, 
		r16, r17, r18, r19, r20, r21, r22, r23, 
		r24, r25, r26, r27, r28, r29, r30, r31		 
	}; 
	
	private List<Integer> program = new ArrayList<>();
	private int start;
	
	public Program(int offset) {
		if ((offset & 3) != 0) {
			throw new IllegalArgumentException("Error: offset is not aligned.");
		}
		start = offset;
	}
	
	public Program() {
		this(0);
	}
	
	public void org(int offset) {
		
		if ((offset & 3) != 0) {
			throw new IllegalArgumentException("Error: offset is not aligned.");
		}
		
		int len = offset - start - (program.size() << 2);		
		if (len < 0) {
			throw new IllegalArgumentException("Error: offset must be more.");
		}
		
		for(int i=0; i<(len >>> 2); i++) {
			program.add(0);
		}
	}
	
	public void l_nop(int K) {
		program.add(0b00010101 << 24 | K);
	}
	
	public void l_movhi(REG D, int K) {
		program.add(0b000110 << 26 | D.ordinal() << 21 | K);
	}
	
	public void l_j(int N) {
		int jump = (N - (program.size() << 2)) >>> 2;
		program.add(jump);
	}
	
	public void l_jalr(REG B) {
		program.add(0b010010 << 26 | B.ordinal() << 11);
	}
	
	public void l_ori(REG D, REG A, int K) {
		program.add(0b101010 << 26 | D.ordinal() << 21 | A.ordinal() << 16 | K);
	}
	
	public void l_jr(REG B) {
		program.add(0b010001 << 26 | B.ordinal() << 11);
	}

	public void l_mfspr(REG D, REG A, int K) {
		program.add(0b101101 << 26 | D.ordinal() << 21 | A.ordinal() << 16 | K);
	}
	
	public void l_mtspr(REG A, REG B, int K) {
		program.add(0b110000 << 26 | ((K >>> 11) & 0x1f) << 21 | A.ordinal() << 16 | B.ordinal() << 11 | (K & 0x7ff));
	}

	public void l_xor(REG A, REG B, REG D) {
		program.add(0b111000 << 26 | D.ordinal() << 21 | A.ordinal() << 16 | B.ordinal() << 11 | 0b00000000101);
	}
	
	
	public byte[] getBytes() {
		byte[] bytes = new byte[program.size() << 2];
		for(int i=0; i<program.size(); i++) {
			bytes[(i << 2) + 0] = (byte) (program.get(i) >>> 24);
			bytes[(i << 2) + 1] = (byte) (program.get(i) >>> 16);
			bytes[(i << 2) + 2] = (byte) (program.get(i) >>> 8);
			bytes[(i << 2) + 3] = (byte) (program.get(i) >>> 0);
		}
		return bytes;
	}

	public static int hi(int val) {
		return (val >>> 16) & 0xffff;
	}
	
	public static int lo(int val) {
		return val & 0xffff;
	}

}
