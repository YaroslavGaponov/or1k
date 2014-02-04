package org.or1k.vm;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.or1k.vm.asm.Program;
import org.or1k.vm.core.Except;
import org.or1k.vm.core.RAM;
import org.or1k.vm.core.VM;
import org.or1k.vm.core.SPR;
import org.or1k.vm.core.FLAG;
import org.or1k.vm.core.device.ATA;
import org.or1k.vm.core.device.Ethernet;
import org.or1k.vm.core.device.FrameBuffer;
import org.or1k.vm.core.device.UART;
import org.or1k.vm.events.Event;
import org.or1k.vm.events.EventEmitter;
import org.or1k.vm.events.EventType;
import org.or1k.vm.gui.term.Terminal;


public class EntryPoint {

	public static void main(String[] args) throws IOException {	
		start(false);
	}

	public static void start(boolean nogui) throws IOException {

		VM vm = new VM(
			new RAM(0x2000000) 
				{{	
					addDevice(0x90000000, 0x7, new UART());
					addDevice(0x91000000, 0x1000, new FrameBuffer());
					addDevice(0x9e000000, 0x1000, new ATA("hd/hdgcc"));
					addDevice(0x92000000, 0x1000, new Ethernet());
				}}
		);

		Path path = Paths.get("kernel/vmlinux");		
		vm.load(0, Files.readAllBytes(path));

		Program boot = new Program() {{								
			// clear r1
			l_xor(REG.r1, REG.r1, REG.r1);

			// set flags
			l_mfspr(REG.r25, REG.r1, SPR.SR);
			l_ori(REG.r25, REG.r25, FLAG.SM.get() | FLAG.FO.get());
			l_mtspr(REG.r1, REG.r25, SPR.SR);

			// set SPR_VR
			l_movhi(REG.r25, hi(0x12000001));
			l_ori(REG.r25, REG.r25, lo(0x12000001));
			l_mtspr(REG.r1, REG.r25, SPR.VR);

			// set SPR_UPR
			l_movhi(REG.r25, hi(0x619));
			l_ori(REG.r25, REG.r25, lo(0x619));
			l_mtspr(REG.r1, REG.r25, SPR.UPR);

			// set SPR_IMMUCFGR and SPR_DMMUCFGR
			l_movhi(REG.r25, hi(0x18));
			l_ori(REG.r25, REG.r25, lo(0x18));
			l_mtspr(REG.r1, REG.r25, SPR.DMMUCFGR);
			l_mtspr(REG.r1, REG.r25, SPR.IMMUCFGR);

			// set SPR_ICCFGR and SPR_DCCFGR
			l_movhi(REG.r25, hi(0x48));
			l_ori(REG.r25, REG.r25, lo(0x48));
			l_mtspr(REG.r1, REG.r25, SPR.ICCFGR);
			l_mtspr(REG.r1, REG.r25, SPR.DCCFGR);

			// interrupt controller mode register (use nmi)
			l_movhi(REG.r25, hi(0x3));
			l_ori(REG.r25, REG.r25, lo(0x3));
			l_mtspr(REG.r1, REG.r25, SPR.PICMR);

			// clear r25
			l_xor(REG.r25, REG.r25, REG.r25);

			// jump to kernel
			l_j(Except.RESET.getAddr());

			// debug point
			l_nop(5);
		}};		
		vm.load(0, boot.getBytes());

		// initialize terminal
		final Terminal term = new Terminal(80, 20);			
		VM.EE.addHandler(EventType.PRINT, new Event() {			
			@Override
			public void handler(Object arg) {				
				term.print((char) arg);
				//System.out.print((char) arg);
			}
		});
		term.addKeyListener(new KeyListener() {			
			@Override
			public void keyTyped(KeyEvent e) {	
				VM.EE.fire(EventType.KEY_TYPED, e.getKeyCode());
			}			
			@Override
			public void keyReleased(KeyEvent e) {
				VM.EE.fire(EventType.KEY_UP, e.getKeyCode());
			}			
			@Override
			public void keyPressed(KeyEvent e) {
				VM.EE.fire(EventType.KEY_DOWN, e.getKeyCode());
			}
		});		

		// start
		if (!nogui) {
			term.enterPrivateMode();		
			try {
				vm.start(0);
			} finally {
				term.exitPrivateMode();
			}	
		} else {
			vm.start(0);
		}
	}

}
