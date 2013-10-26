/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.cogchar.freckbase

import java.sql.{Blob};

object Tuples {
	type Entry		= (Long, Long, Long, Long, Long);
	type Photo		= (Long, Long, Long, Int, Int, Blob);
	type Friend		= (Long, Long, Long, Long, Long, Option[String]);
	type Obs		= (Long, Long, Long, Long, Long, Long, String, Option[String], Option[Long]);
	type Profile	= (Long, Long, Long, String);
	type Attempt	= (Long, Long, Long, Long, Long, Double);
	type Hypo		= (Long, Long, Long, Option[Long]);
}
object PTypes {
	type Entry			= org.cogchar.freckbase.ProfileEntry with Persistent;
	type Photo			= org.cogchar.freckbase.Photo with Persistent;
	type Friend			= org.cogchar.freckbase.Friend with Persistent;
	type Obs			= org.cogchar.freckbase.Observation with Persistent;
	type Profile		= org.cogchar.freckbase.Profile with Persistent;
	type Attempt		= org.cogchar.freckbase.Attempt with Persistent;
	type Hypo			= org.cogchar.freckbase.Hypothesis with Persistent;
}
