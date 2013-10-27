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
 * Restricted API of FaceRec features.
 * @author Stu B. <www.texpedient.com>
 */
public interface FaceRecPopulationManager {

	public long createPopulation();
	public void destroyPopulation(long pop_id);
	public void loadPopulationAndReplaceDefault(String fileName);
	public boolean savePopulation(long popID, String fileName);
	public long getDefaultPopulationID();
	public String matchPerson(OpenCVImage image, long population);
	// public boolean addNamedPerson(OpenCVImage image, String name, long pop_id);
	public boolean addNamedPerson(Collection<OpenCVImage> image, String name, long pop_id);
	public void removePerson(String name, long pop_id);
	public String[] listPopulation(long pop_id);
	
	
}
