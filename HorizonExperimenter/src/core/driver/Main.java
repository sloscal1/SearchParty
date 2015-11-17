package core.driver;

import java.util.Arrays;

import core.party.Dispatcher;
import core.party.Searcher;

/**
 * This is the entry point for the searchparty jar and will split between the
 * client or the server depending on the argument to the program.
 * @author sloscal1
 *
 */
public class Main {
		public static void main(String[] args) throws Exception {
		boolean dispatcher = false;
		//See if this is a dispatcher or searcher
		if(args.length > 0){
			dispatcher = "--dispatcher".startsWith(args[0]) || "-d".equals(args[0]);
			boolean dropArg = dispatcher;
			dropArg |= "--searcher".startsWith(args[0]) || "-s".equals(args[0]);
			//Ditch the parameter that is needed to switch between these
			if(dropArg)
				args = Arrays.copyOfRange(args, 1, args.length);
		}
		
		//Fork to the appropriate part of the program, assume Searcher
		if(dispatcher)
			Dispatcher.main(args);
		else
			Searcher.main(args);
	}
}
