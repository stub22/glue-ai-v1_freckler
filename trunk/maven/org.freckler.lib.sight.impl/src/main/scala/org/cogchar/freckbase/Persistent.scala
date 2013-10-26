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

import java.sql.{Connection, DriverManager, Statement, PreparedStatement, ResultSet, Blob};


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




import java.util.logging.Logger;


trait Persistent extends Record {
/*
	def tableName : String;
	def identColName : String;
	def createStampColName : String = "create_stamp";
	def updateStampColName : String = "update_stamp";
*/
	

	def readTuple(data : Tuple3[Long, Long, Long]) {
		myObjectIdent = Some(data._1);
		myCreateStamp = Some(data._2);
		myUpdateStamp = Some(data._3);
	}
	def readProduct(data : Product) {
		require (data.productArity >= 3);

		val t : Tuple3[Long, Long, Long] = data match {
			case (x1 : Long, x2 : Long, x3: Long) => (x1, x2, x3)
			case (x1 : Long, x2 : Long, x3: Long, _) => (x1, x2, x3)
			case (x1 : Long, x2 : Long, x3: Long, _, _) => (x1, x2, x3)
			case (x1 : Long, x2 : Long, x3: Long, _, _, _) => (x1, x2, x3)
			case (x1 : Long, x2 : Long, x3: Long, _, _, _, _) => (x1, x2, x3)
			case (x1 : Long, x2 : Long, x3: Long, _, _, _, _, _) => (x1, x2, x3)
			case (x1 : Long, x2 : Long, x3: Long, _, _, _, _, _, _) => (x1, x2, x3)
			case (x1 : Long, x2 : Long, x3: Long, _, _, _, _, _, _, _) => (x1, x2, x3)
			case (x1 : Long, x2 : Long, x3: Long, _, _, _, _, _, _, _, _) => (x1, x2, x3)
			case _ => throw new RuntimeException("Bad persistent data tuple: " + data);
		}
//			: (Long, Long, Long) = ((Long) data.productElement(0), (Long) data.productElement(1), (Long) data.productElement(2));
//		}
		readTuple(t);
	}

	override def toString() : String = {
		"Persistent[objID=" + myObjectIdent + "[" + super.toString() + "]]";
	}

}

