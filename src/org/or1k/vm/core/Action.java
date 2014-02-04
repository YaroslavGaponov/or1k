package org.or1k.vm.core;

import java.util.Map;

interface Action {
	void handler(Map<Character, Integer> args);
}
