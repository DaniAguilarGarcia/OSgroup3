package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

/**
 * A <tt>UserProcess</tt> that supports demand-paging.
 */
public class VMProcess extends UserProcess {
    /**
     * Allocate a new process.
     */
    public VMProcess() {
    	super();
    }

    /**
     * Save the state of this process in preparation for a context switch.
     * Called by <tt>UThread.saveState()</tt>.
     */
    public void saveState() {
    	super.saveState();
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
    	//super.restoreState();
    }

    /**
     * Initializes page tables for this process so that the executable can be
     * demand-paged.
     *
     * @return	<tt>true</tt> if successful.
     */
    protected boolean loadSections() {
    	//return super.loadSections();
    	
    	lazyLoader = new LazyLoader(coff);

		start = System.currentTimeMillis();

		return true;
    }

    /**
     * Release any resources allocated by <tt>loadSections()</tt>.
     */
    protected void unloadSections() {
    	//super.unloadSections();
    	
    	coff.close();

		VMKernel.tlbManager.clear();

		for (int i = 0; i < numPages; i++) {
			PageItem item = new PageItem(pid, i);
			Integer ppn = VMKernel.invertedPageTable.remove(item);
			if (ppn != null) {
				VMKernel.memoryManager.removePage(ppn);
				VMKernel.coreMap[ppn].entry.valid = false;
			}

			VMKernel.getSwapper().deleteSwapPage(item);
		}

		Lib.debug(dbgVM, UThread.currentThread().getName() + ", running time: "
				+ (System.currentTimeMillis() - start));
    }    

    /**
     * Handle a user exception. Called by
     * <tt>UserKernel.exceptionHandler()</tt>. The
     * <i>cause</i> argument identifies which exception occurred; see the
     * <tt>Processor.exceptionZZZ</tt> constants.
     *
     * @param	cause	the user exception that occurred.
     */
    public void handleException(int cause) {
		Processor processor = Machine.processor();
	
		switch (cause) {
		case Processor.exceptionTLBMiss:
			int vpn = Processor.pageFromAddress(processor.readRegister(Processor.regBadVAddr));
			PageItem pgItem = new PageItem(pid, vpn);
			int ppn = VMKernel.invertedPageTable.get(pgItem);
			
			if (VMKernel.invertedPageTable.get(pgItem) == null) {
				break;				
			}
			
			TranslationEntry entry = new TranslationEntry(vpn, ppn, true, false, false, false);
			VMKernel.tlbManager.addEntry(entry);
			break;
			
		default:
		    super.handleException(cause);
		    break;
		}
    }
    
    private void handleTLBMissException(int vpn) {
		// Lib.debug(dbgVM, UThread.currentThread().getName() +
		// ", handleTLB miss exception: " + vpn);
    	
    	System.out.println("Test");

		TranslationEntry entry = VMKernel.getPageEntry(new PageItem(pid, vpn));

		if (entry == null) {
			entry = handlePageFault(vpn);
			if (entry == null)
				handleExit(-1);
		}

		VMKernel.tlbManager.addEntry(entry);
	}

	private TranslationEntry handlePageFault(int vpn) {
		lock.acquire();
		numPageFaults++;
		TranslationEntry result = VMKernel.memoryManager.swapIn(new PageItem(pid, vpn), lazyLoader);
		lock.release();
		return result;
	}

	@Override
	protected TranslationEntry getTranslationEntry(int vpn, boolean isWrite) {
		TranslationEntry result = VMKernel.tlbManager.find(vpn, isWrite);
		if (result == null) {
			handleTLBMissException(vpn);
			result = VMKernel.tlbManager.find(vpn, isWrite);
		}
		return result;
	}
	
    private static final int pageSize = Processor.pageSize;
    private static final char dbgProcess = 'a';
    private static final char dbgVM = 'v';
    
    public static int numPageFaults = 0;

	private LazyLoader lazyLoader;
	private long start = 0;
	private static Lock lock = new Lock();
}
