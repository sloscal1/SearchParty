package core.io;

import gnu.getopt.LongOpt;

/**
 * This enum wraps the LongOpt: NO_ARGUMENT, REQUIRED_ARGUMENT, and OPTIONAL_ARGUMENT enum integer values
 * so that other ints cannot accidentally be used when describing command line arguments.
 * @author sloscal1
 *
 */
public enum Arg{
	NONE(LongOpt.NO_ARGUMENT),
	REQUIRED(LongOpt.REQUIRED_ARGUMENT),
	OPTIONAL(LongOpt.OPTIONAL_ARGUMENT);
	private int value;
	private Arg(int longOptVers){
		this.value = longOptVers;
	}
	public int getValue(){return value;}
}
