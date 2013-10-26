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

import java.sql.{Connection, DriverManager, Statement, PreparedStatement, ResultSet, Blob};

import org.cogchar.nwrap.facerec.FaceRecPopulationManager;

import org.cogchar.api.freckler.protocol.{FreckleQuery, FreckleResult, FreckleSampleQuality};
import org.cogchar.api.freckler.protocol.FreckleMatchConfig;
import java.util.logging.Logger;

/* This class provides a facade suitable for Scala 2.7 export to Java.
 * Note that Scala 2.8 has some additional conversion niceties making this export less important.
 */

class Manager(val  mySession : FreckbaseSession) {
	val myLogger = Logger.getLogger(getClass.getName);

	val sqSessionFactory : Database = new FreckbaseSquerySessionFactory(mySession);

	def initTables() {
		import scala.slick.session.Database._
		sqSessionFactory.withSession {
			println(Entries.ddl.createStatements);
			Entries.ddl.create;
			println(Friends.ddl.createStatements);
			Friends.ddl.create;
			println(Photos.ddl.createStatements);
			Photos.ddl.create;
			println(Observations.ddl.createStatements);
			Observations.ddl.create;
			println(Profiles.ddl.createStatements);
			Profiles.ddl.create;
			println(Attempts.ddl.createStatements);
			Attempts.ddl.create;
			println(Hypotheses.ddl.createStatements);
			Hypotheses.ddl.create;
		}
	}
	def reloadPersons() {
		// myPersons = FreckbasePerson.readPersonsAndObservations(getConn());
	}
	def buildPopulation(popMgr : FaceRecPopulationManager, popID : Long) {
		reloadPersons();
		// myPersons.foreach(fp => fp.makeFrecklePerson(popMgr, popID));
	}
	def friendForLegacyFID(legacyFID : String) : Option[Friend] = {
		// myPersons.find(p => (p.myLegacyFreckleID == legacyFID));
		None;
	}
	def friendForID(personID : Long) : Option[Friend] = {
		//myPersons.find(p => (p.myPersonID == personID));
		None;
	}
	def recordObs(ns: Session, frameStamp : Long, hypoID : Long, recogStatus : String,
			imageWidth : Int, imageHeight : Int, imageBytes : Array[Byte]) : Long = {
		val sess = chooseSession(ns);
		val photoID = Photos.insert(imageWidth, imageHeight, imageBytes)(sess);
		val obsID = Observations.insert(frameStamp, photoID, hypoID, recogStatus)(sess);
		obsID;
	}
	def recordHypo(ns: Session) : Long = {
		val sess = chooseSession(ns);
		val hypoID = Hypotheses.insert()(sess);
		hypoID;
	}
	def chooseSession(ns: Session) : Session = {
		if (ns == null) sqSessionFactory.createSession()  else 	ns;
	}
	def storeMatchAltAsAttempt(ns: Session, obsID : Long, matchAlt : FreckleResult.PossibleMatch) : PTypes.Attempt = {
		val sess = chooseSession(ns);
		val matchAttemptScore : Double = matchAlt.getMatchStrength().doubleValue();
		val matchFreckID = matchAlt.getFreckleID();
		// val profile : PTypes.Profile = Profiles.forLegacyFreckleID(profileFreckID)(sess);
		// Strip off the   fbp_
		val textProfileID = matchFreckID.substring(4);
		val parsedProfileID : Long = textProfileID.toLong;
		val profile : PTypes.Profile = Profiles.readOneOrThrow(parsedProfileID)(sess);
		val profileID = profile.myObjectIdent.get;
		val attemptID = Attempts.insert(obsID, profileID, matchAttemptScore)(sess);
		myLogger.info("Inserted Attempt with ID: " + attemptID);
		val ra : PTypes.Attempt = Attempts.readOneOrThrow(attemptID)(sess);
		ra;
	}
	def processFreckleResult(ns: Session, query : FreckleQuery, result : FreckleResult) : PTypes.Obs = {
		
		val sess = chooseSession(ns);
		val obsID : Long = query.getFreckbaseObsID().longValue();
		myLogger.info("freckbase mgr is processing Freckle result for obs: " + obsID);
		val obs : PTypes.Obs = Observations.readOneOrThrow(obsID)(sess);
		myLogger.info("Rehydrated obs: " + obs);
		val hypoID = obs.myHypoID;
		val sampQual : FreckleSampleQuality = result.fetchSampleQuality();
		val sqPacket : Option[String] = if (sampQual != null) Some(sampQual.getRawPacket()) else None;
		var recogStatus = "FAILED";
		var optFriendID : Option[Long] = None;
		val bestAlt = result.fetchBestAlternative();
		
		val matchConfig = query.getFreckleMatchConfig();
		val matchScoreAcceptThresh : Double = matchConfig.getMatchScoreAcceptThresh().doubleValue();
		val matchScoreProfileExpandThresh : Double = matchConfig.getMatchScoreExpandThresh().doubleValue();
		val maxProfileWidth : Int = matchConfig.getMaxProfileWidth().intValue();
		if (bestAlt != null) {
			val att : PTypes.Attempt = storeMatchAltAsAttempt(sess, obsID, bestAlt);
			if (att.myScore > matchScoreAcceptThresh) {
				myLogger.info("Accepted: " + att);
				recogStatus = "MATCH_ACCEPTED";
				val optFriend : Option[PTypes.Friend] = Friends.forProfileID(att.myProfileID)(sess);
				optFriendID = optFriend.map(_.myObjectIdent.get);
				if (att.myScore > matchScoreProfileExpandThresh) {
					myLogger.info("Score " + att.myScore + " exceeds expansion thresh: " + matchScoreProfileExpandThresh);
					if (sampQual.checkEnrollmentWorthy().booleanValue()) {
						myLogger.info("Sample is enrollment-worthy");
						val matchedProfile = Profiles.readOneOrThrow(att.myProfileID)(sess);
						val entryCount : Int = matchedProfile.getEntryCount()(sess);
						// TODO :  Add analysis of timestamps of profile entries,
						// possibly look at match-score history of existing observations.
						if (entryCount < maxProfileWidth) {
							myLogger.info("Replacing old profile with expanded profile.");
							// We rely on there being a fresh freckleID in each query.
							val expLegacyFreckleID = query.getEnrollmentFreckleID();
							val expandedProfileID = Profiles.expand(matchedProfile, expLegacyFreckleID, obs)(sess);
							recogStatus = "MATCHED_AND_PROFILE_EXPANDED";
							// TODO - should this decision be marked on the Attempt?
							Friends.updateFields(optFriend.get, Some(expandedProfileID), None)(sess);
							myLogger.info("Replaced profile " + att.myProfileID + " with expanded profile " + expandedProfileID);
						} else {
							myLogger.info("NOT EXPANDING, because existing profile is at maximum width");
						}
					} else {
						myLogger.info("NOT EXPANDING, because obs " + obsID + " is not enrollment-worthy");
					}
				} else {
					myLogger.info("NOT EXPANDING, because match score is below expansion threshold");
				}
			} else {
				myLogger.info("NOT ACCEPTING, because match score is below acceptance threshold");
			}
		} else {
			myLogger.info("No match alternatives were produced");
		}
		val enrolledLegFreckID = result.getEnrolledFreckleID();
		val enrollAct = result.getEnrollmentAction();
		if (enrolledLegFreckID != null) {
			recogStatus = "ENROLLED";
			val (enrolledProfileID, enrolledFriendID) : (Long, Long) =
					makeFoundingProfileAndFriend(sess, obsID, enrolledLegFreckID);
			optFriendID = Some(enrolledFriendID);
		}
				
		Observations.updateFields(obs, hypoID, recogStatus, sqPacket, optFriendID)(sess);
		obs;
		 /*
		 if (enrolledLegFreckID != null) {
		 val freshFBP = FreckbasePerson.fromFreshData(conn, "stranger", enrolledLegFreckID);
		 reloadPersons();
		 }
		 */
	}
	def makeFoundingProfileAndFriend(ns: Session, foundObsID : Long, legacyFreckID	: String) : (Long, Long) = {
		val sess = chooseSession(ns);
		val obs : PTypes.Obs = Observations.readOneOrThrow(foundObsID)(sess);
		val obsList = List(obs);
		val profileID : Long = Profiles.build(legacyFreckID, obsList)(sess);
		val friendID : Long = Friends.insert(foundObsID, profileID)(sess);
		return (profileID, friendID);
	}
	def makeFriendFromImportedObs(ns: Session, importedObsID : Long) : Long = {
		val sess = chooseSession(ns);
		val legacyFreckleID = "fnd_by_imported_obs_" + importedObsID;
		val (profileID, friendID) = makeFoundingProfileAndFriend(sess, importedObsID, legacyFreckleID);
		val obs : PTypes.Obs = Observations.readOneOrThrow(importedObsID)(sess);
		Observations.updateFields(obs, -1, "IMPORTED_FOUNDER", None, Some(friendID))(sess);
		friendID;
	}
	def expandFriendProfileWithImportedObs(ns: Session, friendID : Long, importedObsID : Long) {
		val sess = chooseSession(ns);
		val friend = getFriend(sess, friendID);
		val preProfile = Profiles.readOneOrThrow(friend.myProfileID)(sess);
		val obs = getObservation(sess, importedObsID);
		val expLegacyFreckleID = "exp_by_imported_obs_" + importedObsID;
		val expandedProfileID = Profiles.expand(preProfile, expLegacyFreckleID, obs)(sess);
		myLogger.info("Expanded profileID=" + expandedProfileID + " for friendID=" + friendID + " with imported obsID=" + importedObsID);
		Friends.updateFields(friend, Some(expandedProfileID), None)(sess);
		Observations.updateFields(obs, -1, "IMPORTED_EXPANDER", None, Some(friendID))(sess);
	}
	def setFriendName(ns: Session, friendID : Long, friendName : String) {
		val sess = chooseSession(ns);
		val friend = getFriend(sess, friendID);
		Friends.updateFields(friend, None, Some(friendName))(sess);
	}
	def removeFriend(ns: Session, friendID : Long) {
		val sess = chooseSession(ns);
		// TODO - purge profile, photos, etc.
		QueryUtils.deleteRow("Friend", friendID)(sess);
	}
	def removeProfileEntry(ns: Session, profileEntryID : Long) {
		val sess = chooseSession(ns);
		// TODO - purge obs, photo
		QueryUtils.deleteRow("Profile_Entry", profileEntryID)(sess);
	}	
	def makeMatchablePopulation(ns: Session, popMgr : FaceRecPopulationManager, popID : Long) : Boolean = {
		val sess = chooseSession(ns);
		val friendList : List[PTypes.Friend] = Friends.listAll()(sess);
		friendList.foreach(f => {
			val profile : Profile = f.getProfile()(sess);
			myLogger.info("Making matchable person for friend " + f + " with profile " + profile);
			profile.makeMatchablePerson(popMgr, popID)(sess);
		});
		true;
	}
	def getPhotoImage(ns: Session, photoID : Long) : java.awt.Image = {
		val sess = chooseSession(ns);
		val ph : PTypes.Photo = Photos.readOneOrThrow(photoID)(sess);
		myLogger.info("Got photo: " + ph);
		ph.fetchJavaImage();
	}
	def getObservation(ns: Session, obsID : Long) : PTypes.Obs = {
		val sess = chooseSession(ns);
		val obs : PTypes.Obs = Observations.readOneOrThrow(obsID)(sess);
		obs;
	}
	def getFriend(ns: Session, friendID : Long) : PTypes.Friend = {
		val sess = chooseSession(ns);
		val friend : PTypes.Friend = Friends.readOneOrThrow(friendID)(sess);
		friend;
	}
	def getAllFriendsInJavaList(ns: Session) : java.util.List[PTypes.Friend] = {
		val sess = chooseSession(ns);
		val javaFriendList = new java.util.ArrayList[PTypes.Friend]();
		val scalaFriendList : List[PTypes.Friend] = Friends.listAll()(sess);
		scalaFriendList.foreach(f => {
			javaFriendList.add(f);
		});
		javaFriendList;
	}
	def getFriendProfileEntriesInJavaList(ns: Session, friendID : Long) : java.util.List[PTypes.Entry] = {
		val sess = chooseSession(ns);
		val friend = getFriend(sess, friendID);
		val profile : PTypes.Profile = Profiles.readOneOrThrow(friend.myProfileID)(sess);
		val profileID : Long = profile.myObjectIdent.get;
		val scalaEntryList : List[PTypes.Entry] = Entries.listForProfile(profileID)(sess);
		val javaEntryList = new java.util.ArrayList[PTypes.Entry]();
		scalaEntryList.foreach(e => {
			javaEntryList.add(e);
		});
		javaEntryList;
	}
	def getOptionalLong(olong: Option[Long], dfault : Long) : Long = {
		return olong.getOrElse({dfault});
	}
	def getOptionalString(ostring: Option[String], dfault: String) : String = {
		return ostring.getOrElse({dfault});
	}
}
