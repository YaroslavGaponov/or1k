package org.or1k.vm.core.device;

import java.util.LinkedList;
import java.util.Queue;

import org.or1k.vm.core.*;
import org.or1k.vm.events.*;

public class UART extends Device implements IDevice8 {
	
	private static final int UART_LCR_INIT = 0b00000011;
	private static final int UART_LSR_INIT = 0b01100000;
	private static final int UART_IIR_INIT = 0b00000001;
	
	private static final int UART_LSR_FIFO_EMPTY = 0x20;
	private static final int UART_LSR_TRANSMITTER_EMPTY = 0x40;
	
	private static final int UART_IIR_THRI	= 0x02;
	private static final int UART_IIR_RDI	= 0x04; 
	private static final int UART_LCR_DLAB	= 0x80; 

	private static final int UART_THR_RBR_DLL = 0;
	private static final int UART_IER_DLM = 1;
	private static final int UART_IIR = 2;
	private static final int UART_LCR = 3;
	private static final int UART_MCR = 4;
	private static final int UART_LSR = 5;
	private static final int UART_MSR = 6;

	private int DLL, IER, DLM, IIR, LCR, MCR, LSR, MSR;
	
	private Queue<Integer> queue = new LinkedList<Integer>();

	public UART() {
		
		reset();

		VM.EE.addHandler(EventType.KEY_TYPED, new Event() {
			@Override
			public void handler(Object arg) {
				queue.add((int)arg);
				IIR |= UART_IIR_RDI;
				VM.EE.fire(EventType.RAISE_INTERRUPT, 2);
			}			
		});
		
	}
	
	public void reset() {
		
		queue.clear();
		
		DLL = IER = DLM = MCR = MSR = 0;
		LCR = UART_LCR_INIT;
		LSR = UART_LSR_INIT;
		IIR = UART_IIR_INIT;
	}


	@Override
	public void write8(int addr, int value) {
		if ((LCR & UART_LCR_DLAB) != UART_LCR_DLAB ) {
			switch(addr) {
				case UART_THR_RBR_DLL:
					LSR &= ~UART_LSR_FIFO_EMPTY;
					IIR |= UART_IIR_THRI; 
					VM.EE.fire(EventType.PRINT, (char)value);
					LSR |= UART_LSR_FIFO_EMPTY;
					VM.EE.fire(EventType.RAISE_INTERRUPT, 2);
					return;
				case UART_IER_DLM:
					IER = value;
					return;
			}
		} else {
			switch(addr) {
				case UART_THR_RBR_DLL:
					DLL = value;
					return;
				case UART_IER_DLM:
					DLM = value;
					return;
			}			
		}		
		switch (addr) {
			case UART_IIR:
				IIR = value;
				break;
			case UART_LCR:
				LCR = value;
				break;
			case UART_MCR:
				MCR = value;
				break;
			case UART_LSR:
				LSR = value;
				break;
			case UART_MSR:
				MSR = value;
				break;
		}
	}

	@Override
	public int read8(int addr) {
		if ((LCR & UART_LCR_DLAB ) != UART_LCR_DLAB) {
			switch(addr) {
				case UART_THR_RBR_DLL:
					VM.EE.fire(EventType.CLEAR_INTERRUPT, 2);
					int kc = 0;
					if (!queue.isEmpty()) {
						kc = queue.poll();
						IIR |= UART_IIR_RDI;
						VM.EE.fire(EventType.RAISE_INTERRUPT, 2);						
					} else {		
						IIR &= ~UART_IIR_RDI;	
					}
					return kc;
				case UART_IER_DLM: return IER;
			}
		} else {
			switch(addr) {
				case UART_THR_RBR_DLL:	return DLL;
				case UART_IER_DLM: 		return DLM;
			}			
		}		
		switch (addr) {
			case UART_IIR: return IIR;
			case UART_LCR: return LCR;
			case UART_MCR: return MCR;
			case UART_LSR: return LSR;
			case UART_MSR: return MSR;
		}
		return 0;
	}

}
