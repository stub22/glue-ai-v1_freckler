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

import java.sql.Blob;
import javax.sql.rowset.serial.SerialBlob;

import org.cogchar.sight.vision.PortableImage;
import org.cogchar.sight.vision.OpenCVImage;

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


case class Photo (	val myImageWidth : Int,
				val myImageHeight : Int,
				val myImageBytes : Array[Byte]) extends Record {

	val myPortableImage : PortableImage = new PortableImage();
	initPI();
	def initPI() {
		myPortableImage.setWidth(myImageWidth);
		myPortableImage.setHeight(myImageHeight);
		myPortableImage.setBytesWithoutHeader(myImageBytes);
	}
	def makeOCVI() : OpenCVImage = {
		val ocvi = myPortableImage.fetchOpenCVImage()
		/*
		val bitmapFilename = "ph_" + myObjectIdent + "_" + java.lang.System.currentTimeMillis() + ".bmp";
		myLogger.info("Writing profile image to" + bitmapFilename);
		ocvi.SaveFile(bitmapFilename);
		*/
		ocvi;
	}
	def fetchJavaImage() : java.awt.Image = {
		myPortableImage.fetchJavaImage();
	}
}

object Photos extends RecordTable[Tuples.Photo, Photo]("Photo") {
	type PersistentPhoto = Photo with Persistent;

	val c_width =		colReqInt("image_width");
	val c_height =		colReqInt("image_height");
	val c_data =		colReqBlob("image_data");

	val reqCols  = stampStar ~ c_width ~ c_height ~ c_data
	override val * = coreStar  ~ c_width ~ c_height ~ c_data

	def bindPersistentPhoto(tup : Tuples.Photo) : PersistentPhoto = {
		val blobby : Blob = tup._6;
		val imageBytes = bytesFromBlob(blobby);
		val p = new Photo(tup._4, tup._5, imageBytes) with Persistent;
		p.readProduct(tup);
		p;
	}
	override def bindTuple(tup : Tuples.Photo) : Photo with Persistent = bindPersistentPhoto(tup);

	def bytesFromBlob(b : Blob) : Array[Byte] = {
		b.getBytes(1, b.length().intValue());
	}
	def blobFromBytes(imageBytes : Array[Byte]) = new SerialBlob(imageBytes);

	def insert  (imageWidth : Int, imageHeight : Int, imageBytes : Array[Byte])
			(implicit isp: Session) : Long = {

		// printArrayDiag(imageBytes, 10000);
		val imageBlob = blobFromBytes(imageBytes);
		val irc = reqCols.insert(-1L, -1L, imageWidth, imageHeight, imageBlob);
		val photoID = QueryUtils.lastInsertedID();
		photoID;
	}
	def test()(implicit isp: Session) : Long = {
		val dummyImage = new Array[Byte](200000);
		val photoID = Photos.insert(94, 49, dummyImage);
		println("New photo ID: " + photoID);
		val ph : PTypes.Photo = Photos.readOneOrThrow(photoID);
		println("Reconstituted photo: " + ph);
		photoID;
	}
	def printArrayDiag(ba : Array[Byte], interval : Int) {
		val len = ba.length;
		val inspectIndices = 0 until (len-1) by interval;
		
		inspectIndices.foreach {(idx : Int) =>
			println("byte[" + idx + "] = " + ba(idx));
		}
		val idx = len - 1;
		println("byte[" + idx + "] = " + ba(idx));
		/*
	   for (idx <- 0 until 5) {
		   println("byte[" + idx + "] = " +  ba(idx));
	   }
	   	*/
	}
	def main(args: Array[String]): Unit = {
		// Bring the implicit session into scope
		// import org.scalaquery.session.SessionFactory._
		import scala.slick.session.Database.threadLocalSession
		println("Photo.main starting");
		val fbs = FreckbaseSession.serverSession();
		val mgr = new Manager(fbs);
		// Force the rec-server native library load
		System.loadLibrary("RobotControlJNIWrapper");
		System.loadLibrary("RecognitionServerJNIWrapper");
	//	import  org.cogchar.FaceRecServer.FaceRecServer;
	//	val dummyFRS = new FaceRecServer("none");
		mgr.sqSessionFactory.withSession {
			val phID = 1;
			val ph  : PTypes.Photo = Photos.readOneOrThrow(phID);
			println("Read: " + ph);
			println("byte len: " + ph.myImageBytes.length);
			// printArrayDiag(ph.myImageBytes, 10000);
			val ocvi : OpenCVImage = ph.makeOCVI();
		}
		fbs.cleanup();
		val bitmapFilename = "C:\\_hanson\\_deploy\\distro_18d\\FH-1268500492796-1000-FMC-10086684.bmp";
		val ocviFromDisk = new OpenCVImage(bitmapFilename, -1);
		// Uhhh...we do not have a way to go from OpenCVImage to a byte array.
		// We are only able to receive these through the callback.
		//
		// val loadedData = // ocviFromDisk.getImageDataNative(); -- not implemented.
		// println("Loaded data of length: " + loadedData.length);
	}
}