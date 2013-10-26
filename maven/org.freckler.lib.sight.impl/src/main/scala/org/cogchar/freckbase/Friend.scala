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



case class Friend (	
				val myFoundingObsID : Long,
				var myProfileID : Long,
				var myPersonName : Option[String]) extends Record {

	def getProfile()(implicit isp: Session) : PTypes.Profile = {
		Profiles.readOneOrThrow(myProfileID);
	}


	def writeName(name : String) {

	}
	def incrementVersion() {

	}
}
object Friends extends RecordTable[Tuples.Friend, Friend]("Friend") {
	
	val c_foundingObsID =	colReqLong("founding_obs_id");
	val c_profileID =		colReqLong("profile_id");
	val c_personName =		colOptString("person_name");

	val reqCols  = stampStar ~ c_foundingObsID ~ c_profileID
	override val * = coreStar ~ c_foundingObsID ~ c_profileID ~ c_personName

	def bindPersistentFriend(ft : Tuples.Friend) : PTypes.Friend = {
		val f = new Friend(ft._4, ft._5, ft._6) with Persistent;
		f.readProduct(ft);
		f;
	}
	override def bindTuple(tup : Tuples.Friend) : PTypes.Friend = bindPersistentFriend(tup);

	def insert(foundObsID : Long, profileID : Long) (implicit isp: Session)  : Long = {
		val rowcount = reqCols.insert(-1L, -1L, foundObsID, profileID);
		println("Inserted friend count: " + rowcount);
		QueryUtils.lastInsertedID();
	}
	def updateFields(friend : PTypes.Friend, profileID : Option[Long], personName : Option[String]) (implicit isp: Session) {
		val friendID : Long = friend.myObjectIdent.get;
		if(profileID.isDefined) {
			QueryUtils.updateValue(tableName, c_profileID.name, friendID, profileID.get);
		}
		if(personName.isDefined) {
			QueryUtils.updateValue(tableName, c_personName.name, friendID, personName.get);
		}
	}
	def forProfileID(profileID : Long)(implicit isp: Session)  : Option[PTypes.Friend] = {
		val q = this where {_.c_profileID is profileID};  // adding ".bind" to obsID makes it a prepared statement
		myLogger.info("Query by profileID: " + q.selectStatement);
		val tup : Tuples.Friend = q.first;
		Some(bindTuple(tup));
		// TODO : Handle query failure and map to None.
	}

	def test(foundingObsID : Long, firstProfileID : Long)(implicit isp: Session) : Long = {
		val friendID = Friends.insert(foundingObsID, firstProfileID);
		println("Inserted Friend with ID: " + friendID);
		val rf : PTypes.Friend = Friends.readOneOrThrow(friendID);
		println("Reconstituted Friend: " + rf);
		Friends.updateFields(rf, None, Some("Buddy Weiser"));
		friendID;
	}



}