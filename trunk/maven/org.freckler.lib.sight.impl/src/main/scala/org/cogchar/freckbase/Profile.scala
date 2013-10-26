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


import org.cogchar.sight.vision.PortableImage;
import org.cogchar.sight.vision.OpenCVImage;
import org.cogchar.nwrap.facerec.FaceRecPopulationManager;

import java.util.logging.Logger;

/* Profile is
 *		1) Immutable after it is built
 *		2) A single version in the sequence of photo collections representing a person.
 *  After some time, a profile may be replaced by a "better" profile, but the old
 *  profile remains persistent in our DB.
 */
case class Profile (val myLegacyFreckleID : String) extends Record {
	
	private var			myCachedEntryList : List[ProfileEntry] = Nil;
	private var			myCachedObsList : List[Observation] = Nil;

	def getEntryList()(implicit isp: Session) :  List[ProfileEntry] = {
		// We need the OID to be populated (e.g. by a Persistent PType.Profile)
		// in order to fetch the entries.
		if (myCachedEntryList == Nil) {
			myCachedEntryList = Entries.listForProfile(myObjectIdent.get);
		}
		myCachedEntryList;
	}
	// TODO:  Consider storing this value as a column (which will be fine, since
	// profile is immutable).   OR, if entryList is not cached, then we could
	// simply do a COUNT(*) query instead of requiring the list to be loaded.
	def getEntryCount()(implicit isp: Session) : Int = {
		val eList = getEntryList();
		eList.size;
	}
	def getObsList()(implicit isp: Session) : List[PTypes.Obs] = {
		val entryList : List[ProfileEntry] = getEntryList();
		entryList.map(_.getObs());
	}

	def makeImageList()(implicit isp: Session) : List[OpenCVImage] = {
		val obsList : List[PTypes.Obs] = getObsList();
		obsList.map {fo =>
			fo.getPhoto().makeOCVI();
		}
	}
	def makeImageJavaCollection()(implicit isp: Session) : java.util.Collection[OpenCVImage] = {
		val scalaList : List[OpenCVImage] = makeImageList();
		// myLogger.info("Profile's scala image list has size: " + scalaList.size);
		// With scala 2.8, we can do a magical type conversion and simply return: scalaList;
		val javaList = new java.util.ArrayList[OpenCVImage]();
		scalaList.foreach {
				javaList.add(_);
		}
		javaList;
	}
	def makeMatchablePerson(popMgr : FaceRecPopulationManager, popID : Long)(implicit isp: Session) : Boolean = {
		val imageColl = makeImageJavaCollection();
		val ident = "fbp_" + myObjectIdent.get; // myObjectIdent must be populated.
		myLogger.info("Adding profile with ID=" + ident + " to pop=" + popID + ", imageCount=" + imageColl.size());
		val nativeResult : Boolean = popMgr.addNamedPerson(imageColl, ident, popID);
		myLogger.info("Native result: " + nativeResult);
		nativeResult;
	}
}
object Profiles extends RecordTable[Tuples.Profile, Profile]("Profile") {

	val c_legacyFreckleID =	colReqString("legacy_freckle_id");

	val insertCols  = stampStar  ~ c_legacyFreckleID
	override val * = coreStar  ~ c_legacyFreckleID

	def bindPersistentProfile(ft : Tuples.Profile) : PTypes.Profile = {
		val f = new Profile(ft._4) with Persistent;
		f.readProduct(ft);
		f;
	}
	override def bindTuple(tup : Tuples.Profile) : PTypes.Profile = bindPersistentProfile(tup);
	def insert(legFreckID : String)(implicit isp: Session)  : Long = {
		val rowcount = insertCols.insert(-1L, -1L, legFreckID);
		myLogger.fine("Inserted profile count: " + rowcount);
		QueryUtils.lastInsertedID();
	}
	def forLegacyFreckleID(legacyFreckID : String)(implicit isp: Session) : PTypes.Profile = {
		val q = this where {_.c_legacyFreckleID is legacyFreckID};
		myLogger.info("Query by legacyFreckleID: " + q.selectStatement);
		val tup : Tuples.Profile = q.first;
		bindTuple(tup);
	}
	def build(legacyFreckID: String, obsList : List[PTypes.Obs])(implicit isp: Session) : Long = {
		val profileID : Long = Profiles.insert(legacyFreckID);
		for (o <- obsList) {
			val entryID = Entries.insert(profileID, o.myObjectIdent.get);
		}
		profileID;
	}
	def expand(oldProfile : PTypes.Profile, expandedFreckID: String, addedObs : PTypes.Obs)(implicit isp: Session) : Long = {
		val oldObsList : List[PTypes.Obs] = oldProfile.getObsList();
		val expandedObsList : List[PTypes.Obs] = addedObs :: oldObsList;
		build(expandedFreckID, expandedObsList);
	}
	def test()(implicit isp: Session) : Long = {
		val profileID = Profiles.insert("legFreck-YOW");
		myLogger.fine("New profile ID: " + profileID);
		val pr : PTypes.Profile = Profiles.readOneOrThrow(profileID);
		myLogger.info("Reconstituted profile: " + pr);
		profileID;
	}
}
