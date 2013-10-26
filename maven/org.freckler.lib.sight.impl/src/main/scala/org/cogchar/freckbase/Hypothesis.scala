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

import java.util.logging.Logger;

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


case class Hypothesis (var myFriendID : Option[Long]) {
}

object Hypotheses extends RecordTable[Tuples.Hypo, Hypothesis]("Hypothesis") {
	val c_friendID	=		colOptLong("friend_id");

	val reqCols  = stampStar
	override val * = coreStar ~ c_friendID

	def bindPersistentHypo(ft : Tuples.Hypo) : PTypes.Hypo = {
		val f = new Hypothesis(ft._4) with Persistent;
		f.readProduct(ft);
		f;
	}
	override def bindTuple(tup : Tuples.Hypo) : PTypes.Hypo = bindPersistentHypo(tup);
	def insert() (implicit isp: Session)  : Long = {
		val rowcount = reqCols.insert(-1L, -1L);
		println("Inserted hypo count: " + rowcount);
		QueryUtils.lastInsertedID();
	}
	def updateFields(hypo : PTypes.Hypo, friendID : Long) (implicit isp: Session) {
		val hypoID : Long = hypo.myObjectIdent.get;
		QueryUtils.updateValue(tableName, c_friendID.name, hypoID, friendID);
		hypo.myFriendID = Some(friendID);
	}
	def test(friendID : Option[Long])(implicit isp: Session) : Long = {
		val hypoID = Hypotheses.insert();
		println("Inserted Hypo with ID: " + hypoID);
		val rh : PTypes.Hypo = Hypotheses.readOneOrThrow(hypoID);
		println("Reconstituted Hypo: " + rh);
		if (friendID.isDefined) {
			Hypotheses.updateFields(rh, friendID.get);
			println("Updated Hypo: " + rh);
			val urh : PTypes.Hypo = Hypotheses.readOneOrThrow(hypoID);
			println("Reconstituted updated hypo: " + urh);
		}
		hypoID;
	}
}
