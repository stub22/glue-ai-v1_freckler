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

import scala.slick.driver._
import scala.slick.lifted._
import scala.slick.lifted.TypeMapper._
import scala.slick.session._

// "Basic" cannot use AutoInc, so we use H2.
//import org.scalaquery.ql.basic.{BasicTable => Table}
//
import scala.slick.driver.{ExtendedProfile, H2Driver}
import scala.slick.driver.H2Driver.Implicit._
import scala.slick.driver.H2Driver.{Table => ExTable}


import java.util.logging.Logger;

/*  Tup is the full tuple of all DB columns, including those inherited from RecordTable.
	Rec is the raw Record type, *before* the Persistent trait is applied.
	TODO:  Write function allowing reuse of column lists
*/
abstract class RecordTable[Tup, Rec](tabName: String) extends ExTable[Tup](tabName) {
	val myLogger = Logger.getLogger(getClass.getName);
	// All tables have an auto-increment column called objec  t_id.
	// We fetch the value of this ID after each insert.
	val c_oid =		column[Long]("object_id", O.AutoInc, O.NotNull);
	val c_cstamp =	colReqLong("create_stamp");
	val c_ustamp =	colReqLong("update_stamp");

	val stampStar = c_cstamp ~ c_ustamp
	val coreStar = c_oid ~ c_cstamp ~ c_ustamp

	def colReqInt(n : String) = column[Int](n, O.NotNull);
	def colOptInt(n : String) = column[Option[Int]](n);
	def colReqLong(n : String) = column[Long](n, O.NotNull);
	def colOptLong(n : String) = column[Option[Long]](n);
	def colReqDouble(n : String) = column[Double](n, O.NotNull);
	def colOptDouble(n : String) = column[Option[Double]](n);
	def colReqString(n : String) = column[String](n, O.NotNull);
	def colOptString(n : String) = column[Option[String]](n);
	def colReqBlob(n : String) = column[Blob](n, O.NotNull);
	def colOptBlob(n : String) = column[Option[Blob]](n);


	def readTupleOrThrow(objID : Long)(implicit isp: Session) : Tup = {
		val q = this where {_.c_oid is objID};  // adding ".bind" to obsID makes it a prepared statement
		println("Query by OID: " + q.selectStatement)
		// Does not permit errors, e.g. bad obsID
		val tup : Tup = q.first;
		tup;
	}
	// abstract - Turn a DB result into a usable record object.
	def bindTuple(t : Tup) : Rec with Persistent;
/*
	def bindSingleQueryResult(q : Query) : Rec with Persistent = {
		null;
	}
*/

	def readOneOrThrow(objID : Long)(implicit isp: Session) : Rec with Persistent = {
		val tup : Tup = readTupleOrThrow(objID);
		bindTuple(tup);
	}
	def printAll()(implicit isp: Session) {
		val q = for(r <- this) yield r.*
		myLogger.info("q: " + q.selectStatement)
		for(tup <- q) {
			myLogger.info("Tuple: " + tup)
			val pr :  Rec with Persistent = bindTuple(tup);
			myLogger.info("Rec: " + pr);
		}
	}
	def listAll()(implicit isp: Session) : List[Rec with Persistent] = {
		val q = for(r <- this) yield r.*
		myLogger.info("q: " + q.selectStatement)
		var resultList : List[Rec with Persistent] = Nil;
		// TODO : there's certainly a better way to do this using map.
		for(tup <- q) {
			// myLogger.info("Tuple: " + tup)
			val pr :  Rec with Persistent = bindTuple(tup);
			// myLogger.info("Rec: " + pr);
			resultList = pr :: resultList;
		}
		resultList;
	}
}