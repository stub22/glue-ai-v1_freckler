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

case class Observation (	val myFrameStamp : Long,
					val myFacePhotoID : Long,
					var myHypoID : Long,
					var myRecogStatus : String,
					var myQualityPacket : Option[String],
					var myFriendID : Option[Long]) extends Record {


	def getPhoto()(implicit isp: Session) : PTypes.Photo = {
		Photos.readOneOrThrow(myFacePhotoID);
	}

}


object Observations extends RecordTable[Tuples.Obs, Observation]("Observation") {

	val c_frameStamp =		colReqLong("frame_stamp");
	val c_facePhotoID	=		colReqLong("face_photo_id");
	val c_hypoID =			colReqLong("hypo_id");
	val c_recogStatus =		colReqString("recog_status");
	val c_qualityPacket =		colOptString("quality_packet");
	val c_friendID =			colOptLong("friend_id");

	val reqCols  = stampStar ~ c_frameStamp ~ c_facePhotoID ~ c_hypoID ~ c_recogStatus
	val writableCols  = reqCols ~ c_qualityPacket ~ c_friendID
	override val * = coreStar ~ c_frameStamp ~ c_facePhotoID ~ c_hypoID ~ c_recogStatus ~ c_qualityPacket ~ c_friendID


	def bindPersistentObs(ot : Tuples.Obs) : PTypes.Obs = {
		val o = new Observation(ot._4, ot._5, ot._6, ot._7, ot._8, ot._9) with Persistent;
		o.readProduct(ot);
		o;
	}
	override def bindTuple(tup : Tuples.Obs) : PTypes.Obs = bindPersistentObs(tup);

	def insert( frameStamp : Long, facePhotoID : Long, hypoID : Long,
					  recogStatus : String)(implicit isp: Session) : Long = {
		val rowcount = reqCols.insert(-1L, -1L, frameStamp, facePhotoID, hypoID, recogStatus);
		println("Inserted observation count: " + rowcount);
		QueryUtils.lastInsertedID();
	}

	def updateFields(obs : PTypes.Obs, hypoID : Long, recogStatus : String,
				qPacket : Option[String], friendID : Option[Long])(implicit isp: Session) {

		// TODO:  Looks like we are not writing the hypoID yet!
		val obsID : Long = obs.myObjectIdent.get; // OrElse{throw new RuntimeException("No obs ID")};
		QueryUtils.updateValue(tableName, c_recogStatus.name, obsID, recogStatus);
		if(qPacket.isDefined) {
			QueryUtils.updateValue(tableName, c_qualityPacket.name, obsID, qPacket.get);
		}
		if(friendID.isDefined) {
			QueryUtils.updateValue(tableName, c_friendID.name, obsID, friendID.get);
		}
		obs.myRecogStatus = recogStatus;
		obs.myQualityPacket = qPacket;
		obs.myFriendID = friendID;
	}
	// def readObservationsForFPID(conn : Connection, freckledPersonID : Int) : List[FreckbaseObservation] = {
	// 	FOREIGN KEY (freckled_person_id) REFERENCES Freckled_Person(freckled_person_id)
	// quality_packet VARCHAR(1000)

	def test(mgr : Manager, photoID : Long, hypoID : Long) (implicit isp: Session) : Long = {
		val obsID = Observations.insert(9393939, photoID, hypoID, "FRESH");
		println("Inserted obs with ID: " + obsID);
		val obs : PTypes.Obs = Observations.readOneOrThrow(obsID);
		println("Reconstituted obs: " + obs);
		Observations.updateFields(obs, 773322, "HEALTHY", Some("qualitas"), None);
		obsID;
	}
}