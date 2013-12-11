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

package org.cogchar.freckbase;
import java.sql.{Connection, DriverManager, Statement, PreparedStatement, ResultSet, Blob};
import org.h2.tools.Server;
import java.util.logging.Logger;

class FreckbaseSession (val myDriverClassName : String, val myURL : String, val myUser : String, val myPass : String) {
	import FreckbaseSession.theLogger;
	private var myConn : Connection = null;
	private var myTcpServer : Server = null;

	def getConn() : Connection = {
		val sqlTimeoutSec = 1;
		if (myConn != null) {
			if (!myConn.isValid(sqlTimeoutSec)) {
				theLogger.info("myConn isValid returned false, closing.");
				myConn.close();
				theLogger.info("myConn closed, nulling.");
				myConn = null;
			} else {
				theLogger.info("existing myConn is valid");
			}
		}
		if (myConn == null) {
			myConn = makeConn();
		}
		myConn;
	}
	def makeConn() : Connection = {
		theLogger.info("Connecting to DB at url " + myURL);
		Class.forName(myDriverClassName).newInstance;
		DriverManager.getConnection(myURL, myUser, myPass);
	}
	def startTCPServer(tcpPort : String) {
		theLogger.info("Creating TCP server on port  " + tcpPort);
		val myTcpServer = Server.createTcpServer("-tcpAllowOthers", "true", "-tcpPort", tcpPort);
		theLogger.info("Starting TCP server");
		myTcpServer.start();
	}
	def cleanup() {
		if (myTcpServer != null) {
			theLogger.info("Stopping TCP server");
			myTcpServer.stop();
			myTcpServer = null;
		}
		if (myConn != null) {
			theLogger.info("Closing DB connection");
			myConn.close();
			myConn = null;
		}
	}
}
class Cache[C <: AnyRef] {
	val myItems =  new scala.collection.mutable.HashMap[Long, C];
	def findItem(id : Long) : Option[C] = {
		if (myItems.contains(id)) {
			Some(myItems(id));
		} else {
			/*

			 http://www.scala-lang.org/node/2206   -- Martin O. sez:
			 Null is a subtype of any class or trait that inherits of AnyRef. But
			 it's not automatically a subtype of a type parameter that's bounded by
			 an AnyRef. So the behavior you are seeing is as expected.
			 */
			None;
		}
	}
	def insertItem(id : Long, obj : C) {
		myItems += (id -> obj);
	}
}
object FreckbaseSession {
	val theLogger = Logger.getLogger(getClass.getName);

	val theDriverClassName = "org.h2.Driver";
	// Note that theDbFilePath is a var which can be changed.
	var theDbFilePath = "FILE_PATH_GOES_HERE"; // C:/_hanson/_deploy/freckbase_01/h2db02";
	val theDbUser = "sa";
	val theDbPassword = "";
	val theTcpPort = "8043";
	val theServerProtocol = "jdbc:h2:file:";
	val theClientProtocol = "jdbc:h2:tcp://localhost:";

	val	myPhotoCache = new Cache[Photo];


	def clientURL () : String = {
		theClientProtocol +  theTcpPort + "/" + theDbFilePath;
	}
	def serverURL () : String = {
		theServerProtocol + theDbFilePath;
	}
	def clientSession() : FreckbaseSession = {
		new FreckbaseSession(theDriverClassName, clientURL(), theDbUser, theDbPassword);
	}
	def serverSession() : FreckbaseSession = {
		new FreckbaseSession(theDriverClassName, serverURL(), theDbUser, theDbPassword);
	}
	def configureDatabaseFilePath(path : String) = {
		theLogger.info("Setting Freckbase H2 DB file path to: " + path);
		theDbFilePath = path;
	}

}
class FreckbaseSquerySessionFactory (val myFBS : FreckbaseSession)
		extends scala.slick.session.Database {
	override def createConnection(): Connection = myFBS.getConn();
}