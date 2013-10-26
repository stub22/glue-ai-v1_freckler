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

import scala.slick.driver._
import scala.slick.lifted._
import scala.slick.lifted.TypeMapper._
import scala.slick.session._

// "Basic" cannot use AutoInc, so we use H2.
//import org.scalaquery.ql.basic.{BasicTable => Table}
//
import scala.slick.driver.{ExtendedProfile, H2Driver}
import scala.slick.driver.H2Driver.{Table => ExTable}
import scala.slick.driver.H2Driver.Implicit._

object TestSQ {

	def main(args: Array[String]): Unit = {
		// Bring the implicit session into scope
		import scala.slick.session.Database.threadLocalSession
		// import org.scalaquery.session.SessionFactory._
		println("TestSQ starting");
		val fbs = FreckbaseSession.serverSession();
		val mgr = new Manager(fbs);
		mgr.initTables();
		// withSession sets up the implicit session in Session factory
		mgr.sqSessionFactory.withSession {
			val photoID = Photos.test();
			val hypoID = Hypotheses.test(None);
			val obsID = Observations.test(mgr, photoID, hypoID);
			val profileID = Profiles.test();

			val entryID = Entries.test(profileID, obsID);
			val friendID = Friends.test(obsID, profileID);
			val attemptID = Attempts.test(obsID, profileID);


			Friends.printAll();
			Observations.printAll();
			Entries.printAll();
			Attempts.printAll();
			Hypotheses.printAll();
			Photos.printAll();
			Profiles.printAll();

			if (true) {
				println("Starting TCP Server");
				fbs.startTCPServer(FreckbaseSession.theTcpPort);
				println("Napping for 20 minutes");
				Thread.sleep(20 * 60 * 1000);
			}
			println("Naptime is over, cleaning up before exit.");
			fbs.cleanup();
		}
		
		println("TestSQ ending");
	}

	def testExplicitSession(mgr : Manager) {
		val dummyImage = new Array[Byte](186000);
		val explicitSession = scala.slick.session.Database.threadLocalSession; // SessionFactory.getThreadSession;
		val fullObsID = mgr.recordObs(explicitSession, 777, 8484, "SPIFFY",	1024, 768, dummyImage);
		println("Recorded full obs with ID: " + fullObsID);
		val rfo : PTypes.Obs = Observations.readOneOrThrow(fullObsID)(explicitSession);
		println("Reconstituted full obs: " + rfo);
		QueryUtils.updateValue(Observations.tableName, Observations.c_recogStatus.name, fullObsID, "FIXED")(explicitSession);
		//	fullObsID;
	}
}
