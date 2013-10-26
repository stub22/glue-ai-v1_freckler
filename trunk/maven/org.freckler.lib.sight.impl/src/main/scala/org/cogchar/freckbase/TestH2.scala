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

import java.sql.{Connection, DriverManager, Statement, CallableStatement, PreparedStatement, ResultSet};
import org.cogchar.api.freckler.protocol.{FreckleQuery, FreckleResult};
import org.h2.tools.Server;
import java.util.logging.Logger;

class TestH2(dbFilePath : String, dbUser : String, dbPassword : String,  tcpPort : String) {
	val myTcpServer = Server.createTcpServer("-tcpAllowOthers", "true", "-tcpPort", tcpPort);
	var myConn = loadConn();
	start();

	def ensureGoodConnection() {
	}
	def loadConn() : Connection = {
		// Load the driver
		Class.forName("org.h2.Driver").newInstance;
		DriverManager.getConnection("jdbc:h2:file:" + dbFilePath, dbUser, dbPassword);
	}
	def start() {
		myTcpServer.start();
	}
	def getConn() : Connection = {
		// TODO:  Test connection and reopen if needed
		myConn;
	}
	def cleanup() {
		myTcpServer.stop();
		myConn.close();
	}
	def getLastInsertID(conn : Connection) : Long = {
		/* H2 can prepareCall and execute, but cannot (yet) register output types
		 * or fetch output values

		val cst = conn.prepareCall("CALL IDENTITY()");
		cst.registerOutParameter(1, java.sql.Types.BIGINT);
		val exr : Boolean = cst.execute();
		val res : Long = cst.getLong(1);
		cst.close();
		 */
		var lastInsertID : Long = -1;
		val st = conn.createStatement();
		val sxr = st.execute("SELECT IDENTITY()");
		val rs = st.getResultSet();
		if (rs.next()) {
			lastInsertID = rs.getLong(1);
		}
		rs.close();
		st.close();
		lastInsertID;
	}
	def testH2() {
		val conn : Connection = getConn();
		val st : Statement = conn.createStatement();
		val br1 : Boolean = st.execute("DROP TABLE IF EXISTS Test");
		println("drop table returned ", br1);
		val br2 : Boolean = st.execute("CREATE TABLE Test(ID INT IDENTITY, name VARCHAR(255), stamp TIMESTAMP)");
		println("create table returned ", br2);
		val br3 : Boolean = st.execute("INSERT INTO Test (name, stamp) VALUES ('Server sez hello', NOW())");
		val insertID : Long = getLastInsertID(conn);
		println("getLastInsertID returned: ", insertID);
		val br5 : Boolean = st.execute("SELECT * FROM Test");
		println("query returned ", br5);
		val rs : ResultSet = st.getResultSet();
		while (rs.next()) {
			println (rs.getLong("ID"));
			println (rs.getString("NAME"));
			println (rs.getTimestamp("STAMP"));
		}
		rs.close();
		st.close();
		conn.commit();
	}
	/*  client testing code
  def getConn() : Connection = {
    val url = "jdbc:h2:tcp://localhost:" + FreckbaseServer.theTcpPort + "/" + FreckbaseServer.theDbFilePath;
    println("Connecting to url " + url);
    val conn : Connection = DriverManager.getConnection(url, FreckbaseServer.theDbUser, FreckbaseServer.theDbPassword);
    conn;
  }
	  def main(args: Array[String]): Unit = {
    becomeSomewhatJiggy();
    println("Freckbase client starting");
    val conn : Connection = getConn();
    try {
      val st : Statement = conn.createStatement();
      val br3 : Boolean = st.execute("INSERT INTO Test (name, stamp) VALUES('Client sez yo!', NOW())");
      conn.commit();
    }
    finally {
      conn.close();
    }

  }

	*/

}
object TestH2 {
	val theDbFilePath = "C:/_hanson/_deploy/freckbase_01/h2db01";
	val theDbUser = "sa";
	val theDbPassword = "";
	val theTcpPort = "8043";

	val theLogger = Logger.getLogger(getClass.getName);

	def makeServer() : TestH2 = {
		new TestH2(theDbFilePath, theDbUser, theDbPassword, theTcpPort);
	}
	def main(args: Array[String]): Unit = {
		println("TestH2 singleton.main() starting");
		val fs = makeServer();
		try {
			fs.testH2();
			// fs.initFaceTables();
			Thread.sleep(20*60*1000);
		}
		finally {
			fs.cleanup();
		}
	}
}
