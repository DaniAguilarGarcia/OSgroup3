package nachos.vm;

import nachos.machine.Lib;
import nachos.machine.Machine;
import nachos.machine.TranslationEntry;

public class TLBManager {
	
	public void addEntry(TranslationEntry entry) {
		int index = -1;
		
		for (int i = 0; i < Machine.processor().getTLBSize(); i++) {
			if (!Machine.processor().readTLBEntry(i).valid) {
				index = 1;
				break;
			}
		}
		
		if (index == -1) {
			index = Lib.random(Machine.processor().getTLBSize());
		}
		
		Machine.processor().writeTLBEntry(index, entry);
	}

}
