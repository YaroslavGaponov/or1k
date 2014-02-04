package org.or1k.vm.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventEmitter {
	
	private Map<EventType, List<Event>> events = new HashMap<EventType, List<Event>>();
	
	private Object locker = new Object();
	
	public void addHandler(EventType type, Event event) {
		synchronized(locker) {
			if (!events.containsKey(type)) {
				events.put(type, new ArrayList<Event>());
			}
			events.get(type).add(event);
		}
	}
	
	public void fire(EventType type, Object arg) {
		synchronized(locker) {
			if (events.containsKey(type)) {
				for(Event e : events.get(type)) {
					e.handler(arg);
				}
			}
		}
	}
		
		
}
