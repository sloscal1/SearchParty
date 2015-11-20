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

package core.party;

import core.messages.SearcherCentricMessages.RunSettings;

/**
 * The implementing class is able to provide RunSettings to send out to
 * an interested Searcher.
 * 
 * @author sloscal1
 *
 */
public interface ExpGenerator {
	RunSettings next();
	boolean hasNext();
}
