package org.or1k.vm.core;

import java.util.HashMap;
import java.util.Map;

enum Opcode {
	
	l_j			("000000NNNNNNNNNNNNNNNNNNNNNNNNNN"),
	l_jal		("000001NNNNNNNNNNNNNNNNNNNNNNNNNN"),
	l_bnf		("000011NNNNNNNNNNNNNNNNNNNNNNNNNN"),
	l_bf		("000100NNNNNNNNNNNNNNNNNNNNNNNNNN"),
	l_nop		("00010101--------KKKKKKKKKKKKKKKK"),
	l_movhi		("000110DDDDD----0KKKKKKKKKKKKKKKK"),
	l_macrc		("000110DDDDD----10000000000000000"),
	l_sys		("0010000000000000KKKKKKKKKKKKKKKK"), 
	l_trap		("0010000100000000KKKKKKKKKKKKKKKK"), 
	l_msync		("00100010000000000000000000000000"),
	l_psync		("00100010100000000000000000000000"),
	l_csync		("00100011000000000000000000000000"), 
	l_rfe		("001001--------------------------"), 
	lv_cust1	("001010------------------1100----"), 
	lv_cust2	("001010------------------1101----"),
	lv_cust3	("001010------------------1110----"), 
	lv_cust4	("001010------------------1111----"), 
	lv_all_eq_b	("001010DDDDDAAAAABBBBB---00010000"),
	lv_all_eq_h	("001010DDDDDAAAAABBBBB---00010001"), 
	lv_all_ge_b	("001010DDDDDAAAAABBBBB---00010010"), 
	lv_all_ge_h	("001010DDDDDAAAAABBBBB---00010011"), 
	lv_all_gt_b	("001010DDDDDAAAAABBBBB---00010100"), 
	lv_all_gt_h	("001010DDDDDAAAAABBBBB---00010101"), 
	lv_all_le_b	("001010DDDDDAAAAABBBBB---00010110"), 
	lv_all_le_h	("001010DDDDDAAAAABBBBB---00010111"), 
	lv_all_lt_b	("001010DDDDDAAAAABBBBB---00011000"), 
	lv_all_lt_h	("001010DDDDDAAAAABBBBB---00011001"), 
	lv_all_ne_b	("001010DDDDDAAAAABBBBB---00011010"), 
	lv_all_ne_h	("001010DDDDDAAAAABBBBB---00011011"), 
	lv_any_eq_b	("001010DDDDDAAAAABBBBB---00100000"), 
	lv_any_eq_h	("001010DDDDDAAAAABBBBB---00100001"),
	lv_any_ge_b	("001010DDDDDAAAAABBBBB---00100010"), 
	lv_any_ge_h	("001010DDDDDAAAAABBBBB---00100011"), 
	lv_any_gt_b	("001010DDDDDAAAAABBBBB---00100100"), 
	lv_any_gt_h	("001010DDDDDAAAAABBBBB---00100101"), 
	lv_any_le_b	("001010DDDDDAAAAABBBBB---00100110"), 
	lv_any_le_h	("001010DDDDDAAAAABBBBB---00100111"), 
	lv_any_lt_b	("001010DDDDDAAAAABBBBB---00101000"), 
	lv_any_lt_h	("001010DDDDDAAAAABBBBB---00101001"), 
	lv_any_ne_b	("001010DDDDDAAAAABBBBB---00101010"), 
	lv_any_ne_h	("001010DDDDDAAAAABBBBB---00101011"), 
	lv_add_b	("001010DDDDDAAAAABBBBB---00110000"), 
	lv_add_h	("001010DDDDDAAAAABBBBB---00110001"), 
	lv_adds_b	("001010DDDDDAAAAABBBBB---00110010"), 
	lv_adds_h	("001010DDDDDAAAAABBBBB---00110011"), 
	lv_addu_b	("001010DDDDDAAAAABBBBB---00110100"), 
	lv_addu_h	("001010DDDDDAAAAABBBBB---00110101"), 
	lv_addus_b	("001010DDDDDAAAAABBBBB---00110110"), 
	lv_addus_h	("001010DDDDDAAAAABBBBB---00110111"), 
	lv_and		("001010DDDDDAAAAABBBBB---00111000"), 
	lv_avg_b	("001010DDDDDAAAAABBBBB---00111001"), 
	lv_avg_h	("001010DDDDDAAAAABBBBB---00111010"), 
	lv_cmp_eq_b	("001010DDDDDAAAAABBBBB---01000000"), 
	lv_cmp_eq_h	("001010DDDDDAAAAABBBBB---01000001"), 
	lv_cmp_ge_b	("001010DDDDDAAAAABBBBB---01000010"), 
	lv_cmp_ge_h	("001010DDDDDAAAAABBBBB---01000011"), 
	lv_cmp_gt_b	("001010DDDDDAAAAABBBBB---01000100"), 
	lv_cmp_gt_h	("001010DDDDDAAAAABBBBB---01000101"), 
	lv_cmp_le_b	("001010DDDDDAAAAABBBBB---01000110"), 
	lv_cmp_le_h	("001010DDDDDAAAAABBBBB---01000111"), 
	lv_cmp_lt_b	("001010DDDDDAAAAABBBBB---01001000"), 
	lv_cmp_lt_h	("001010DDDDDAAAAABBBBB---01001001"), 
	lv_cmp_ne_b	("001010DDDDDAAAAABBBBB---01001010"), 
	lv_cmp_ne_h	("001010DDDDDAAAAABBBBB---01001011"), 
	lv_madds_h	("001010DDDDDAAAAABBBBB---01010100"), 
	lv_max_b	("001010DDDDDAAAAABBBBB---01010101"), 
	lv_max_h	("001010DDDDDAAAAABBBBB---01010110"), 
	lv_merge_b	("001010DDDDDAAAAABBBBB---01010111"), 
	lv_merge_h	("001010DDDDDAAAAABBBBB---01011000"), 
	lv_min_b	("001010DDDDDAAAAABBBBB---01011001"), 
	lv_min_h	("001010DDDDDAAAAABBBBB---01011010"), 
	lv_msubs_h	("001010DDDDDAAAAABBBBB---01011011"),
	lv_muls_h	("001010DDDDDAAAAABBBBB---01011100"), 
	lv_nand		("001010DDDDDAAAAABBBBB---01011101"), 
	lv_nor		("001010DDDDDAAAAABBBBB---01011110"), 
	lv_or		("001010DDDDDAAAAABBBBB---01011111"), 
	lv_pack_b	("001010DDDDDAAAAABBBBB---01100000"), 
	lv_pack_h	("001010DDDDDAAAAABBBBB---01100001"), 
	lv_packs_b	("001010DDDDDAAAAABBBBB---01100010"), 
	lv_packs_h	("001010DDDDDAAAAABBBBB---01100011"), 
	lv_packus_b	("001010DDDDDAAAAABBBBB---01100100"), 
	lv_packus_h	("001010DDDDDAAAAABBBBB---01100101"), 
	lv_perm_n	("001010DDDDDAAAAABBBBB---01100110"), 
	lv_rl_b		("001010DDDDDAAAAABBBBB---01100111"), 
	lv_rl_h		("001010DDDDDAAAAABBBBB---01101000"), 
	lv_sll_b	("001010DDDDDAAAAABBBBB---01101001"),
	lv_sll_h	("001010DDDDDAAAAABBBBB---01101010"), 
	lv_sll		("001010DDDDDAAAAABBBBB---01101011"), 
	lv_srl_b	("001010DDDDDAAAAABBBBB---01101100"), 
	lv_srl_h	("001010DDDDDAAAAABBBBB---01101101"), 
	lv_sra_b	("001010DDDDDAAAAABBBBB---01101110"), 
	lv_sra_h	("001010DDDDDAAAAABBBBB---01101111"), 
	lv_srl		("001010DDDDDAAAAABBBBB---01110000"), 
	lv_sub_b	("001010DDDDDAAAAABBBBB---01110001"), 
	lv_sub_h	("001010DDDDDAAAAABBBBB---01110010"), 
	lv_subs_b	("001010DDDDDAAAAABBBBB---01110011"), 
	lv_subs_h	("001010DDDDDAAAAABBBBB---01110100"), 
	lv_subu_b	("001010DDDDDAAAAABBBBB---01110101"), 
	lv_subu_h	("001010DDDDDAAAAABBBBB---01110110"), 
	lv_subus_b	("001010DDDDDAAAAABBBBB---01110111"), 
	lv_subus_h	("001010DDDDDAAAAABBBBB---01111000"), 
	lv_unpack_b	("001010DDDDDAAAAABBBBB---01111001"), 
	lv_unpack_h	("001010DDDDDAAAAABBBBB---01111010"), 
	lv_xor		("001010DDDDDAAAAABBBBB---01111011"), 
	l_jr		("010001----------BBBBB-----------"), 
	l_jalr		("010010----------BBBBB-----------"), 
	l_maci		("010011-----AAAAAIIIIIIIIIIIIIIII"),
	l_cust1		("011100--------------------------"), 
	l_cust2		("011101--------------------------"), 
	l_cust3		("011110--------------------------"), 
	l_cust4		("011111--------------------------"), 
	l_ld		("100000DDDDDAAAAAIIIIIIIIIIIIIIII"), 
	l_lwz		("100001DDDDDAAAAAIIIIIIIIIIIIIIII"), 
	l_lws		("100010DDDDDAAAAAIIIIIIIIIIIIIIII"), 
	l_lbz		("100011DDDDDAAAAAIIIIIIIIIIIIIIII"), 
	l_lbs		("100100DDDDDAAAAAIIIIIIIIIIIIIIII"), 
	l_lhz		("100101DDDDDAAAAAIIIIIIIIIIIIIIII"), 
	l_lhs		("100110DDDDDAAAAAIIIIIIIIIIIIIIII"), 
	l_addi		("100111DDDDDAAAAAIIIIIIIIIIIIIIII"), 
	l_addic		("101000DDDDDAAAAAIIIIIIIIIIIIIIII"), 
	l_andi		("101001DDDDDAAAAAKKKKKKKKKKKKKKKK"), 
	l_ori		("101010DDDDDAAAAAKKKKKKKKKKKKKKKK"), 
	l_xori		("101011DDDDDAAAAAIIIIIIIIIIIIIIII"), 
	l_muli		("101100DDDDDAAAAAIIIIIIIIIIIIIIII"), 
	l_mfspr		("101101DDDDDAAAAAKKKKKKKKKKKKKKKK"), 
	l_slli		("101110DDDDDAAAAA--------00LLLLLL"), 
	l_srli		("101110DDDDDAAAAA--------01LLLLLL"), 
	l_srai		("101110DDDDDAAAAA--------10LLLLLL"), 
	l_rori		("101110DDDDDAAAAA--------11LLLLLL"), 
	l_sfeqi		("10111100000AAAAAIIIIIIIIIIIIIIII"), 
	l_sfnei		("10111100001AAAAAIIIIIIIIIIIIIIII"), 
	l_sfgtui	("10111100010AAAAAIIIIIIIIIIIIIIII"), 
	l_sfgeui	("10111100011AAAAAIIIIIIIIIIIIIIII"), 
	l_sfltui	("10111100100AAAAAIIIIIIIIIIIIIIII"),
	l_sfleui	("10111100101AAAAAIIIIIIIIIIIIIIII"), 
	l_sfgtsi	("10111101010AAAAAIIIIIIIIIIIIIIII"), 
	l_sfgesi	("10111101011AAAAAIIIIIIIIIIIIIIII"), 
	l_sfltsi	("10111101100AAAAAIIIIIIIIIIIIIIII"), 
	l_sflesi	("10111101101AAAAAIIIIIIIIIIIIIIII"), 
	l_mtspr		("110000KKKKKAAAAABBBBBKKKKKKKKKKK"), 
	l_mac		("110001-----AAAAABBBBB-------0001"), 
	l_macu		("110001-----AAAAABBBBB-------0011"), 
	l_msb		("110001-----AAAAABBBBB-------0010"), 
	l_msbu		("110001-----AAAAABBBBB-------0100"), 
	lf_sfeq_s	("110010-----AAAAABBBBB---00001000"), 
	lf_sfne_s	("110010-----AAAAABBBBB---00001001"), 
	lf_sfgt_s	("110010-----AAAAABBBBB---00001010"), 
	lf_sfge_s	("110010-----AAAAABBBBB---00001011"), 
	lf_sflt_s	("110010-----AAAAABBBBB---00001100"), 
	lf_sfle_s	("110010-----AAAAABBBBB---00001101"), 
	lf_sfeq_d	("110010-----AAAAABBBBB---00011000"), 
	lf_sfne_d	("110010-----AAAAABBBBB---00011001"), 
	lf_sfgt_d	("110010-----AAAAABBBBB---00011010"), 
	lf_sfge_d	("110010-----AAAAABBBBB---00011011"), 
	lf_sflt_d	("110010-----AAAAABBBBB---00011100"), 
	lf_sfle_d	("110010-----AAAAABBBBB---00011101"), 
	lf_cust1_s	("110010-----AAAAABBBBB---1101----"), 
	lf_cust1_d	("110010-----AAAAABBBBB---1110----"), 
	lf_itof_s	("110010DDDDDAAAAA00000---00000100"), 
	lf_ftoi_s	("110010DDDDDAAAAA00000---00000101"), 
	lf_itof_d	("110010DDDDDAAAAA00000---00010100"), 
	lf_ftoi_d	("110010DDDDDAAAAA00000---00010101"), 
	lf_add_s	("110010DDDDDAAAAABBBBB---00000000"), 
	lf_sub_s	("110010DDDDDAAAAABBBBB---00000001"), 
	lf_mul_s	("110010DDDDDAAAAABBBBB---00000010"), 
	lf_div_s	("110010DDDDDAAAAABBBBB---00000011"), 
	lf_rem_s	("110010DDDDDAAAAABBBBB---00000110"), 
	lf_madd_s	("110010DDDDDAAAAABBBBB---00000111"), 
	lf_add_d	("110010DDDDDAAAAABBBBB---00010000"), 
	lf_sub_d	("110010DDDDDAAAAABBBBB---00010001"), 
	lf_mul_d	("110010DDDDDAAAAABBBBB---00010010"), 
	lf_div_d	("110010DDDDDAAAAABBBBB---00010011"), 
	lf_rem_d	("110010DDDDDAAAAABBBBB---00010110"), 
	lf_madd_d	("110010DDDDDAAAAABBBBB---00010111"), 
	l_sd		("110100IIIIIAAAAABBBBBIIIIIIIIIII"), 
	l_sw		("110101IIIIIAAAAABBBBBIIIIIIIIIII"), 
	l_sb		("110110IIIIIAAAAABBBBBIIIIIIIIIII"), 
	l_sh		("110111IIIIIAAAAABBBBBIIIIIIIIIII"), 
	l_exths		("111000DDDDDAAAAA------0000--1100"), 
	l_extws		("111000DDDDDAAAAA------0000--1101"), 
	l_extbs		("111000DDDDDAAAAA------0001--1100"), 
	l_extwz		("111000DDDDDAAAAA------0001--1101"), 
	l_exthz		("111000DDDDDAAAAA------0010--1100"), 
	l_extbz		("111000DDDDDAAAAA------0011--1100"), 
	l_add		("111000DDDDDAAAAABBBBB-00----0000"), 
	l_addc		("111000DDDDDAAAAABBBBB-00----0001"), 
	l_sub		("111000DDDDDAAAAABBBBB-00----0010"), 
	l_and		("111000DDDDDAAAAABBBBB-00----0011"), 
	l_or		("111000DDDDDAAAAABBBBB-00----0100"),
	l_xor		("111000DDDDDAAAAABBBBB-00----0101"), 
	l_cmov		("111000DDDDDAAAAABBBBB-00----1110"), 
	l_ff1		("111000DDDDDAAAAA------00----1111"), 
	l_sll		("111000DDDDDAAAAABBBBB-0000--1000"), 
	l_srl		("111000DDDDDAAAAABBBBB-0001--1000"), 
	l_sra		("111000DDDDDAAAAABBBBB-0010--1000"), 
	l_ror		("111000DDDDDAAAAABBBBB-0011--1000"), 
	l_fl1		("111000DDDDDAAAAA------01----1111"), 
	l_mul		("111000DDDDDAAAAABBBBB-11----0110"), 
	l_muld		("111000-----AAAAABBBBB-11----0111"), 
	l_div		("111000DDDDDAAAAABBBBB-11----1001"), 
	l_divu		("111000DDDDDAAAAABBBBB-11----1010"), 
	l_mulu		("111000DDDDDAAAAABBBBB-11----1011"), 
	l_muldu		("111000-----AAAAABBBBB-11----1100"), 
	l_sfeq		("11100100000AAAAABBBBB-----------"), 
	l_sfne		("11100100001AAAAABBBBB-----------"), 
	l_sfgtu		("11100100010AAAAABBBBB-----------"), 
	l_sfgeu		("11100100011AAAAABBBBB-----------"), 
	l_sfltu		("11100100100AAAAABBBBB-----------"), 
	l_sfleu		("11100100101AAAAABBBBB-----------"), 
	l_sfgts		("11100101010AAAAABBBBB-----------"), 
	l_sfges		("11100101011AAAAABBBBB-----------"), 
	l_sflts		("11100101100AAAAABBBBB-----------"), 
	l_sfles		("11100101101AAAAABBBBB-----------"), 
	l_cust5		("111100DDDDDAAAAABBBBBLLLLLLKKKKK"), 
	l_cust6		("111101--------------------------"), 
	l_cust7		("111110--------------------------"), 
	l_cust8		("111111--------------------------");
	
	
	private final String pattern;
	private final int[] mask = new int[2];
	
	Opcode(String pattern) {
		this.pattern = pattern;
		
		for(int i=0; i<pattern.length(); i++) {
			mask[0] <<= 1; mask[1] <<= 1;
			switch (pattern.charAt(i)) {				
				case '1': mask[1] |= 1;
				case '0': mask[0] |= 1;
			}
		}
	}
	
	public String getPattern() {
		return this.pattern;
	}
	
	public boolean equals(int code) {
		return ((code & mask[0]) == mask[1]);
	}
	
	public static Opcode getOpcode(int code) {
		Opcode[] values = Opcode.values();
		for(int i=0; i<values.length; i++) {
			if (values[i].equals(code)) {
				return values[i];
			}
		}		
		throw new IllegalArgumentException("Opcode is not found.");
	}
	
	public static Map<Character, Integer> getParams(Opcode op, int code) {		
		Map<Character, Integer> params = new HashMap<Character, Integer>(); 
		
		StringBuilder sb = new StringBuilder(Integer.toBinaryString(code));
		for(int i=0; i<Integer.numberOfLeadingZeros(code); i++) {
			sb.insert(0, '0');
		}
		String bs = sb.toString();
		
		String pattern = op.getPattern();

		for(int i=0; i<pattern.length(); i++) {
			char name = pattern.charAt(i); 
			if (Character.isLetter(name)) {
				int value = 0;
				if (params.containsKey(name)) {
					value = params.get(name) << 1;
				}
				if (bs.charAt(i) == '1') {
					value |= 1;
				}
				params.put(name, value);
			}
		}
		
		for(Character name: params.keySet()) {
			switch (name) {
				case 'A': case 'B': case 'D': case 'L':
					params.put(name, params.get(name) & 0x1f);
					break;
				case 'K':
					params.put(name, params.get(name) & 0xffff);
					break;
				case 'I':
					params.put(name, (params.get(name) << 16) >> 16);
					break;
				case 'N':
					params.put(name, (params.get(name) << 6) >> 4);
					break;
				default:
					throw new IllegalArgumentException(String.format("Parameter with type '%s' is not supported.", name));
			}
		}
		
		return params;
	}

	
}

