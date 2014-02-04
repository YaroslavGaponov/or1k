package org.or1k.vm.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Timer;

import org.or1k.vm.events.*;
import org.or1k.vm.exceptions.*;

public class VM {
	
	public static final EventEmitter EE = new EventEmitter();
	
	private Map<Opcode, Action> handlers = new HashMap<Opcode, Action>();
	  		
	private int pc;
	private int npc;
	private boolean delay;
	
	private RAM ram;
	
	private int[] r = new int[32];
	private SPR spr = new SPR();
	private SR sr = new SR();
		
	private TIMER timer = new TIMER();
	
	private List<Integer> bp = new ArrayList<Integer>();
		
	public VM(RAM ram) {
		this.ram = ram;
		
		initialize();		
		
		Timer t = new Timer(100, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Utils.getBit(timer.TTMR, 30)) {
	                int tick = (timer.TTMR & 0xFFFFFFF) - (timer.TTCR & 0xFFFFFFF);
	                if (tick < 0) tick += 0xFFFFFFF;
					timer.TTCR = (timer.TTCR + 1000) & 0xFFFFFFF;
	                if (tick <= 1000) {
	                    if (Utils.getBit(timer.TTMR, 29)) {
	                        timer.TTMR |= (1 << 28); 
	                    }
	                }
	            }		
			}
			
		});
		t.start();
	}
	
	public void addHandler(Opcode op, Action handler) {
		handlers.put(op, handler);
	}
	
	private void step(Opcode op, Map<Character, Integer> params) {
		if (! handlers.containsKey(op)) {
			throw new IllegalArgumentException(String.format("Handler for %s is not found.", op));
		}
		
		handlers.get(op).handler(params);				
	}
	
	
	public void start(int start) {
		
	    pc = start; 
		npc = pc << 2;
		delay = false;
		

		int times = 100;
		
		while(true) {			
			try {
				

				if (--times == 0) {
					times = 100;
				
					if (checkForInterrupt()) {
						throw new INTException(spr.get(SPR.EEAR_BASE));
		            }
					
		            if ((sr.get(FLAG.TEE) && Utils.getBit(timer.TTMR, 28))) {
		            	throw new TICKException(spr.get(SPR.EEAR_BASE));
		            } 

				}
	            
				
				int code = ram.read32(getInstAddr(pc)); 
				Opcode op = Opcode.getOpcode(code);
				Map<Character, Integer> params = Opcode.getParams(op, code);
				
				String info = String.format("0x%08x\t%s\t%s", pc, op, params);
		
				for(Integer addr: bp) {
					if (addr == pc) {
						bp.remove(addr);
						REPL(info);
						break;
					}
				}
				
				
				if (op.equals(Opcode.l_sys)) {
					throw new SYSException(spr.get(SPR.EEAR_BASE));
				}
			
				int oldpc = pc;
				step(op, params);
				if (oldpc == pc) {
					next();
				}
				
				
			} catch (ITLBMISSException e) {
				except(athrow(Except.ITLBMISS, pc - (delay ? 4: 0), e.getAddr()));
			} catch (IPFException e) { 
				except(athrow(Except.IPF, pc - (delay ? 4: 0), e.getAddr()));
			} catch (INTException e) { 
				except(athrow(Except.INT, pc - (delay ? 4: 0), e.getAddr()));
			} catch (TICKException e) { 
				except(athrow(Except.TICK, pc - (delay ? 4: 0), e.getAddr()));
			} catch(SYSException e) {
				except(athrow(Except.SYSCALL, pc + 4 - (delay ? 4: 0), e.getAddr()));
			}
			
		}				
	}
	
	
	public void REPL(String info) {
		System.out.println(pc + " " + info);
		System.out.println("DEBUG: r[egister], f[lag], b[reakpoint], g[o], m[emory], q[uit]");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while(true)
		try {
			System.out.print(": ");
			String line  = in.readLine();
			
			// trace
			if (line == null || line.isEmpty()) {
				bp.add(npc);
				break;
			}
			
			String[] p = line.split(" ");
			switch(p[0]) {
				case "r": // print register(s)
					if (p.length == 1) {
						for(int idx=0; idx<r.length; idx++) {
							System.out.println(String.format("r[%s] = 0x%08x", idx, r[idx]));
						} 
					} else {
						for(int i=1; i<p.length; i++){
							try {
								int idx = Integer.parseInt(p[i]);
								System.out.println(String.format("r[%s] = 0x%08x", idx, r[idx]));
							} catch (Exception ex) {}
						}							
					}					
					break;
				case "f": // print flag
					if (p.length == 1) {
						for(FLAG flag : FLAG.values()) {
							System.out.println(String.format("SR.%s = %s", flag, sr.get(flag)));
						}
					} else {
						for(int i=1; i<p.length; i++) {
							try {
								FLAG flag = FLAG.valueOf(p[i]);
								System.out.println(String.format("SR.%s = %s", flag, sr.get(flag)));
							} catch(Exception ex) {}
						}							
					}											
					break;					
				case "b": // set breakpoint
					for(int i=1; i<p.length; i++) {
						try {
							int addr =  Integer.parseInt(p[i], 16);
							bp.add(addr);
						} catch (Exception ex) {}
					}
					break;
				case "m":
					for(int i=1; i<p.length; i++) {
						try {
							int addr =  Integer.parseInt(p[i], 16);
							System.out.println(String.format("memory[0x%08x] = 0x%08x", addr, ram.read32(addr)));
						} catch (Exception ex) {
							ex.printStackTrace();
						}	
					}
					break;
				case "g": // go
					return;
				case "q": // exit
					System.exit(0);
			}
		} catch (IOException e) {}
	}
		
	public void load(int offset, byte[] data) {	
		for(int i=0; i<data.length; i++) {
			ram.write8u(offset + i, data[i]);
		}
	}
		
	public void next(int pc, int npc, boolean delay) {
		this.pc = pc;
		this.npc = npc;
		this.delay = delay;
	}	
	public void next() {
		next(this.npc, this.npc + 4, false);
	}
	public void jump(int npc) {
		next(this.npc, npc, true);
	}
	public void except(int pc) {
		next(pc, pc + 4, false);
	}

	
	private int check(long n) {
		if (n < Integer.MIN_VALUE || n > Integer.MAX_VALUE) {
			throw new ArithmeticException("Integer Overflow");
		}
		return (int)n;
	}
	
	private void initialize() {
		
		EE.addHandler(EventType.RAISE_INTERRUPT, new Event() {			
			@Override
			public void handler(Object arg) {
				raiseInterrupt((int) arg);
			}
		});

		EE.addHandler(EventType.CLEAR_INTERRUPT, new Event() {			
			@Override
			public void handler(Object arg) {
				clearInterrupt((int) arg);
			}
		});		
		
		EE.addHandler(EventType.DEBUG, new Event() {

			@Override
			public void handler(Object arg) {
				REPL("debug");
				
			}		
		});
		
		EE.addHandler(EventType.KEY_DOWN, new Event() {
			@Override
			public void handler(Object arg) {	
				if ((int)arg == 9) {
					bp.add(npc);
				}
			}
			
		});
		
		addHandler(Opcode.l_nop, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {	
			}
		});		
		
		addHandler(Opcode.l_movhi, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {				
				r[args.get('D')] = args.get('K') << 16;
				
			}
		});

		addHandler(Opcode.l_ori, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				r[args.get('D')] = r[args.get('A')] | args.get('K');
				
			}
		});		

		addHandler(Opcode.l_add, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {				
				try {
					long res = r[args.get('A')] + r[args.get('B')];
					r[args.get('D')] = check(res);
		            sr.set(FLAG.CY, true);
		            sr.set(FLAG.OV, false);					
				} catch (ArithmeticException ex) {
		            sr.set(FLAG.CY, true);
		            sr.set(FLAG.OV, true);										
				} catch (Exception ex) {
		            sr.set(FLAG.CY, false);
		            sr.set(FLAG.OV, false);															
				}				
			}
		});		

		addHandler(Opcode.l_jr, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) { 
				jump(r[args.get('B')]);
			}
		});		

		addHandler(Opcode.l_or, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				r[args.get('D')] = r[args.get('A')] | r[args.get('B')];				
			}
		});		
		
		addHandler(Opcode.l_mtspr, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				int idx = r[args.get('A')] | args.get('K');				
				switch (idx) {
					case SPR.NPC:
						npc = r[args.get('B')];
						break;
					case SPR.SR:						
						sr.setFlags(r[args.get('B')]);						
						break;
					case TIMER.TIMER_TTCR:
						timer.TTCR = r[args.get('B')];
						break;
					case TIMER.TIMER_TTMR:
						timer.TTMR = r[args.get('B')];
						break;
					default:
						spr.set(idx, r[args.get('B')]);
						break;
				}
				
			}
		});		
		
		addHandler(Opcode.l_mfspr, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				int idx = r[args.get('A')] | args.get('K');
				switch (idx) {
					case SPR.NPC:
						r[args.get('D')] = npc;
						break;
					case SPR.SR:
						r[args.get('D')] = sr.getFlags();
						break;
					case TIMER.TIMER_TTCR:
						r[args.get('D')] = timer.TTCR;
						break;
					case TIMER.TIMER_TTMR:
						r[args.get('D')] = timer.TTMR;
						break;
					default:
						r[args.get('D')] = spr.get(idx);
						break;
				}
					
			}
		});		


		addHandler(Opcode.l_sw, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				try {
					int addr = getDataAddr(r[args.get('A')] + args.get('I'), true);
					ram.write32(addr, r[args.get('B')]);					  						
				} catch (DTLBMISSException e) {
					except(athrow(Except.DTLBMISS, delay ? (pc - 4) : pc, e.getAddr()));
				} catch (DPFException e) {
					except(athrow(Except.DPF, delay ? (pc - 4) : pc, e.getAddr()));
				}
			}
		});		
		
		addHandler(Opcode.l_sfltu, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, Utils.uint(r[args.get('A')]) < Utils.uint(r[args.get('B')]));				
			}
		});		

		addHandler(Opcode.l_bf, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {				
				if (sr.get(FLAG.F)) {					
					jump(pc + args.get('N'));
				}				
			}
		});		
		
		addHandler(Opcode.l_addi, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {				
				try {
					long res = r[args.get('A')] + args.get('I');
					r[args.get('D')] = check(res);
		            sr.set(FLAG.CY, true);
		            sr.set(FLAG.OV, false);
				} catch (ArithmeticException ex) {
		            sr.set(FLAG.CY, true);
		            sr.set(FLAG.OV, true);					
				} catch (Exception ex) {
		            sr.set(FLAG.CY, false);
		            sr.set(FLAG.OV, false);										
				}	            
			}
		});		

		addHandler(Opcode.l_jal, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				r[9] = npc + 4;
				jump(pc + args.get('N'));
			}
		});		
		
		addHandler(Opcode.l_andi, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				r[args.get('D')] = r[args.get('A')] & args.get('K');
				
			}
		});		

		addHandler(Opcode.l_sfeq, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, r[args.get('A')] == r[args.get('B')]);				
			}
		});		

		addHandler(Opcode.l_bnf, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {				
				if (!sr.get(FLAG.F)) {	
					jump(pc + args.get('N'));
				} 			
			}
		});		

		addHandler(Opcode.l_j, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				jump(pc + args.get('N'));
			}
		});		

		addHandler(Opcode.l_srli, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				r[args.get('D')] = r[args.get('A')] >>> args.get('L');				
			}
		});		

		addHandler(Opcode.l_sll, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				r[args.get('D')] = r[args.get('A')] << r[args.get('B')];				
			}
		});	
		
		addHandler(Opcode.l_and, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				r[args.get('D')] = r[args.get('A')] & r[args.get('B')];				
			}
		});			
		
		addHandler(Opcode.l_sfgeu, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, Utils.uint(r[args.get('A')]) >= Utils.uint(r[args.get('B')]));				
			}
		});	
		
		addHandler(Opcode.l_lwz, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				try {
					int addr = getDataAddr(r[args.get('A')] + args.get('I'), false);
					r[args.get('D')] = ram.read32(addr);					  					
				} catch (DTLBMISSException e) {
					except(athrow(Except.DTLBMISS, delay ? (pc - 4) : pc, e.getAddr()));
				} catch (DPFException e) {
					except(athrow(Except.DPF, delay ? (pc - 4) : pc, e.getAddr()));
				}
			}
		});	
		
		addHandler(Opcode.l_rfe, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
	            sr.setFlags(spr.get(SPR.ESR_BASE));
				int _pc = spr.get(SPR.EPCR_BASE);
				next(_pc, _pc + 4, false);
			}
		});
		
		addHandler(Opcode.l_jalr, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				r[9] = npc + 4;
				jump(r[args.get('B')]);
			}
		});		
		

		addHandler(Opcode.l_sfeqi, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, r[args.get('A')] == args.get('I'));		
			}
		});		
		
		addHandler(Opcode.l_sfnei, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, r[args.get('A')] != args.get('I'));				
			}
		});		

		addHandler(Opcode.l_lbs, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				try {
					int addr = getDataAddr(r[args.get('A')] + args.get('I'), false);
					r[args.get('D')] = ram.read8s(addr);
				} catch (DTLBMISSException e) {
					except(athrow(Except.DTLBMISS, delay ? (pc - 4) : pc, e.getAddr()));
				} catch (DPFException e) {
					except(athrow(Except.DPF, delay ? (pc - 4) : pc, e.getAddr()));
				}
			}
		});	
		

		addHandler(Opcode.l_xori, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				r[args.get('D')] = r[args.get('A')] ^ args.get('I');				
			}
		});		

		addHandler(Opcode.l_sub, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {				
				try {
					long res = r[args.get('A')] - r[args.get('B')];
					r[args.get('D')] = check(res); 
		            sr.set(FLAG.CY, true);
		            sr.set(FLAG.OV, false);
				} catch (ArithmeticException ex) {
		            sr.set(FLAG.CY, true);
		            sr.set(FLAG.OV, true);					
				} catch (Exception ex) {
		            sr.set(FLAG.CY, false);
		            sr.set(FLAG.OV, false);										
				}
			}
		});			
		

		addHandler(Opcode.l_sfltsi, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, r[args.get('A')] < args.get('I'));				
			}
		});		
		

		addHandler(Opcode.l_sfgtui, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, Utils.uint(r[args.get('A')]) > Utils.uint(args.get('I')));				
			}
		});		
		
		addHandler(Opcode.l_lbz, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {				
				try {
					int addr = getDataAddr(r[args.get('A')] + args.get('I'), false);
					r[args.get('D')] = ram.read8u(addr);
				} catch (DTLBMISSException e) {
					except(athrow(Except.DTLBMISS, delay ? (pc - 4) : pc, e.getAddr()));
				} catch (DPFException e) {
					except(athrow(Except.DPF, delay ? (pc - 4) : pc, e.getAddr()));
				}
			}
		});	

		addHandler(Opcode.l_sfleu, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, Utils.uint(r[args.get('A')]) <= Utils.uint(r[args.get('B')]));				
			}
		});		

		addHandler(Opcode.l_sfleui, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, Utils.uint(r[args.get('A')]) <= Utils.uint(args.get('I')));				
			}
		});		
		

		addHandler(Opcode.l_sfgtu, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, Utils.uint(r[args.get('A')]) > Utils.uint(r[args.get('B')]));				
			}
		});		

		addHandler(Opcode.l_sb, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				try {
					int addr = getDataAddr(r[args.get('A')] + args.get('I'), true);
					ram.write8s(addr, r[args.get('B')]);					  						
				} catch (DTLBMISSException e) {
					except(athrow(Except.DTLBMISS, delay ? (pc - 4) : pc, e.getAddr()));
				} catch (DPFException e) {
					except(athrow(Except.DPF, delay ? (pc - 4) : pc, e.getAddr()));
				}
			}
		});		
		
		addHandler(Opcode.l_sfgeui, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, Utils.uint(r[args.get('A')]) >= Utils.uint(args.get('I')));				
			}
		});		
		

		addHandler(Opcode.l_slli, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				r[args.get('D')] = r[args.get('A')] << args.get('L');				
			}
		});		

		addHandler(Opcode.l_sfgesi, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, r[args.get('A')] >= args.get('I'));				
			}
		});		
		
		addHandler(Opcode.l_sfne, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, r[args.get('A')] != r[args.get('B')]);				
			}
		});		


		addHandler(Opcode.l_sfles, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, r[args.get('A')] <= r[args.get('B')]);				
			}
		});		
		

		addHandler(Opcode.l_sh, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				try {
					int addr = getDataAddr(r[args.get('A')] + args.get('I'), true);
					ram.write16s(addr, r[args.get('B')]);					
				} catch (DTLBMISSException e) {
					except(athrow(Except.DTLBMISS, delay ? (pc - 4) : pc, e.getAddr()));
				} catch (DPFException e) {
					except(athrow(Except.DPF, delay ? (pc - 4) : pc, e.getAddr()));
				}
			}
		});		
		
		addHandler(Opcode.l_sfgts, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, r[args.get('A')] > r[args.get('B')]);				
			}
		});		
		
		addHandler(Opcode.l_sflts, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, r[args.get('A')] < r[args.get('B')]);				
			}
		});	
		
		addHandler(Opcode.l_lhs, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				try {
					int addr = getDataAddr(r[args.get('A')] + args.get('I'), false);
					r[args.get('D')] = ram.read16s(addr);					  						
				} catch (DTLBMISSException e) {
					except(athrow(Except.DTLBMISS, delay ? (pc - 4) : pc, e.getAddr()));
				} catch (DPFException e) {
					except(athrow(Except.DPF, delay ? (pc - 4) : pc, e.getAddr()));
				}
			}
		});	

		addHandler(Opcode.l_lhz, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				try {
					int addr = getDataAddr(r[args.get('A')] + args.get('I'), false);
					r[args.get('D')] = ram.read16u(addr);
				} catch (DTLBMISSException e) {
					except(athrow(Except.DTLBMISS, delay ? (pc - 4) : pc, e.getAddr()));
				} catch (DPFException e) {
					except(athrow(Except.DPF, delay ? (pc - 4) : pc, e.getAddr()));
				}
			}
		});	
		
		addHandler(Opcode.l_sfges, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, r[args.get('A')] >= r[args.get('B')]);				
			}
		});	

		addHandler(Opcode.l_srai, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				r[args.get('D')] = r[args.get('A')] >> args.get('L');				
			}
		});		

		
		addHandler(Opcode.l_mul, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {				
				try {
					long res = r[args.get('A')] * r[args.get('B')];
					r[args.get('D')] = check(res);
		            sr.set(FLAG.CY, true);
		            sr.set(FLAG.OV, false);
				} catch (ArithmeticException ex) {
		            sr.set(FLAG.CY, true);
		            sr.set(FLAG.OV, true);					
				} catch (Exception ex) {
		            sr.set(FLAG.CY, false);
		            sr.set(FLAG.OV, false);										
				}
			}
		});	

		addHandler(Opcode.l_xor, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {				
				r[args.get('D')] = r[args.get('A')] ^ r[args.get('B')];				
			}
		});			
		
		addHandler(Opcode.l_sflesi, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {				
				sr.set(FLAG.F, r[args.get('A')] <= args.get('I'));				
			}
		});			

		addHandler(Opcode.l_srl, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				r[args.get('D')] = r[args.get('A')] >>> r[args.get('B')];
			}
		});	
		

		addHandler(Opcode.l_divu, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {				
				try {
					long res = (Utils.uint(r[args.get('A')]) / Utils.uint(r[args.get('B')]));
					r[args.get('D')] = check(res);
	                sr.set(FLAG.CY, true);
	                sr.set(FLAG.OV, false);
				} catch(ArithmeticException ex) {
	                sr.set(FLAG.CY, true);
	                sr.set(FLAG.OV, true);					
				} catch(Exception ex) {
					sr.set(FLAG.CY, false);
					sr.set(FLAG.OV, false);
				}
				
			}
		});		
		
		addHandler(Opcode.l_ff1, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {				
				int val = r[args.get('A')];
				r[args.get('D')] = 0;
				for(int i=0; i<32; i++) {
					if ((val & 1) != 0) {
						r[args.get('D')] = i + 1;
						break;
					}
					val >>>= 1; 
				}				
			}
		});	
		
		
		addHandler(Opcode.l_fl1, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {				
				int val = r[args.get('A')];
				r[args.get('D')] = 0;
				for(int i=31; i>=0; i--) {
					if ((val & (1<<i)) != 0) {
						r[args.get('D')] = i + 1;
						break;
					}
				}				
			}
		});	
		
		
		addHandler(Opcode.l_sfgtsi, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, r[args.get('A')] > args.get('I'));				
			}
		});	
		
		addHandler(Opcode.l_div, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {				
				try {
					long res = r[args.get('A')] / r[args.get('B')];
					r[args.get('D')] = check(res);
	                sr.set(FLAG.CY, true);
	                sr.set(FLAG.OV, false);
				} catch(ArithmeticException ex) {
	                sr.set(FLAG.CY, true);
	                sr.set(FLAG.OV, true);					
				} catch (Exception ex) {
	                sr.set(FLAG.CY, false);
	                sr.set(FLAG.OV, false);										
				}
			}
		});
		
		addHandler(Opcode.l_sra, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				r[args.get('D')] = r[args.get('A')] >> r[args.get('B')];				
			}
		});		
		
		addHandler(Opcode.l_sfltui, new Action() {
			@Override
			public void handler(Map<Character, Integer> args) {
				sr.set(FLAG.F, Utils.uint(r[args.get('A')]) < Utils.uint(args.get('I')));
			}
		});	
		
	}
			
	private int athrow(Except handler, int pc, int addr) {
						
	    spr.set(SPR.EPCR_BASE, pc);				
	    spr.set(SPR.EEAR_BASE, addr);
	    spr.set(SPR.ESR_BASE, sr.getFlags());
	    
	    sr.set(FLAG.OVE, false);
	    sr.set(FLAG.SM,	 true);
	    sr.set(FLAG.IEE, false);
	    sr.set(FLAG.TEE, false);
	    sr.set(FLAG.DME, false);
	    sr.set(FLAG.IME, false);
	    
	    int exAddr = handler.getAddr();
	    if (sr.get(FLAG.EPH)) {
	    	exAddr |= 0xf0000000;
	    }
	    
		return exAddr;
	}
	
	
	private int getDataAddr(int addr, boolean write) throws DTLBMISSException, DPFException {
		
		if (!sr.get(FLAG.DME)) {
			return addr;
		}
		
		int idx = (addr >>> 13) & 0x3f;
		
		int tlmbr = spr.get(1, idx | 0x200);
	    if (((tlmbr & 1) == 0) || ((tlmbr >>> 19) != (addr >>> 19))) {
	        throw new DTLBMISSException(addr);	        	        
	    }
	    
	    int tlbtr = spr.get(1, idx | 0x280);
	    if (sr.get(FLAG.SM)) {
	        if ((!write && ((tlbtr & 0x100) == 0)) || (write && ((tlbtr & 0x200) == 0))) { 
	        	throw new DPFException(addr); 
	        }
	    } else {
	        if ((!write && ((tlbtr & 0x40) == 0)) || (write && ((tlbtr & 0x80) == 0))) { 
	        	throw new DPFException(addr); 
	        }
	    }
	    
	    return ((tlbtr & 0xFFFFE000) | (addr & 0x1FFF));
	}
	
	private int getInstAddr(int addr) throws ITLBMISSException, IPFException {
		
		if (!sr.get(FLAG.IME)) {
			return addr;
		}

	    int idx = (addr >>> 13) & 0x3f;
	    
	    int tlmbr = spr.get(2, idx | 0x200);
	    if (((tlmbr & 1) == 0) || ((tlmbr >>> 19) != (addr >>> 19))) {
	    	throw new ITLBMISSException(addr);
	    }
	    
	    int tlbtr = spr.get(2, idx | 0x280);
	    if (sr.get(FLAG.SM)) {
	        if ((tlbtr & 0x40) == 0) {
	        	throw new IPFException(addr);
	        }
	    } else {
	        if ((tlbtr & 0x80) == 0) {
	        	throw new IPFException(addr);	        
	        }
	    }
	    
	    return (tlbtr & 0xFFFFE000) | (addr & 0x1FFF);	    
	}	
	
	private void raiseInterrupt(int line) {
	    int picsr = spr.get(SPR.PICSR);
	    picsr |= 1 << line;
	    spr.set(SPR.PICSR, picsr);
	};	
	
	private void clearInterrupt(int line) {
	    int picsr = spr.get(SPR.PICSR);
	    picsr &= ~(1 << line);
	    spr.set(SPR.PICSR, picsr);
	};
	
	private boolean checkForInterrupt() {
	    if (sr.get(FLAG.IEE)) {
	    	int picmr = spr.get(SPR.PICMR);
	    	int picsr = spr.get(SPR.PICSR);
	    	return (( picmr & picsr ) != 0);	    			    		       
	    }
	    return false;
	};
	
}
