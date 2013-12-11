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

case class ProfileEntry(val myProfileID : Long, val myObsID : Long) extends Record {
	def getObs()(implicit isp: Session) : PTypes.Obs = {
		Observations.readOneOrThrow(myObsID);
	}
}

object Entries extends RecordTable[Tuples.Entry, ProfileEntry]("Profile_Entry") {

	val c_profileID =	colReqLong("profile_id");
	val c_obsID =		colReqLong("obs_id");
	val reqCols = stampStar ~ c_profileID ~ c_obsID
	override val * = coreStar ~ c_profileID ~ c_obsID

	def bindPersistentEntry(tup : Tuples.Entry) : PTypes.Entry = {
		val pe = new ProfileEntry(tup._4, tup._5) with Persistent;
		pe.readProduct(tup);
		pe;
	}
	override def bindTuple(tup : Tuples.Entry) :  PTypes.Entry = bindPersistentEntry(tup);

	def insert(profileID : Long, obsID : Long)(implicit isp: Session) : Long = {
		val rowcount = reqCols.insert(-1L, -1L, profileID, obsID);
		println("Inserted entry count: " + rowcount);
		QueryUtils.lastInsertedID();
	}
	def listForProfile(profileID : Long)(implicit isp: Session) : List[PTypes.Entry] = {
		var entryList : List[PTypes.Entry] = Nil;
		val q = for(r <- this where {_.c_profileID is profileID}) yield r.*
		println("q: " + q.selectStatement)
		for(tup <- q) {
			val entry :  PTypes.Entry = bindTuple(tup);
			entryList = entry :: entryList;
		}
		entryList;
	}
	def test(profileID : Long, obsID : Long)(implicit isp: Session) : Long = {
		val eid = Entries.insert(profileID, obsID);
		println("Inserted entry with ID: " + eid);
		val entry : PTypes.Entry = Entries.readOneOrThrow(eid);
		println("Reconstituted entry: " + entry);
		eid;
	}
}