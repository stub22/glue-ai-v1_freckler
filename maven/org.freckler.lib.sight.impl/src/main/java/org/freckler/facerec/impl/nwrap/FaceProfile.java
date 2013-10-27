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


package org.freckler.facerec.impl.nwrap;

import org.cogchar.sight.api.obs.OpenCVImage;
import java.util.Collection;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class FaceProfile {
	private	Collection<OpenCVImage>		myOCVImages;
	private	Long						myFIR_NativePointer;

	public FaceProfile(Collection<OpenCVImage> images) {
		myOCVImages = images;
	}
	public long[] getNativeImagePointerArray() {
		long imagePtrs[] = new long[myOCVImages.size()];
		int idx = 0;
		for (OpenCVImage ocvi: myOCVImages) {
			imagePtrs[idx++] = ocvi.raw();
		}
		return imagePtrs;
	}
	public void setFIR_NativePointer(Long fir_np) {
		myFIR_NativePointer = fir_np;
	}
	public Long getFIR_NativePointer() {
		return myFIR_NativePointer;
	}
}
