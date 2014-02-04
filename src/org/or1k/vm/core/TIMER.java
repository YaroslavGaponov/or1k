package org.or1k.vm.core;

import java.util.Timer;

public class TIMER {
	
	public static final int TIMER_TTMR = (10<<11)|0;
	public static final int TIMER_TTCR = (10<<11)|1;
	
	public volatile int TTMR;
	public volatile int TTCR;

}
