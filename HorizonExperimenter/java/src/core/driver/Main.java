/*
Copyright 2015 Steven Loscalzo

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
*/

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
