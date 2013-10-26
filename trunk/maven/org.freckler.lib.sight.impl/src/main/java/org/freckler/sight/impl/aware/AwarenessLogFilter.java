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

package org.freckler.sight.impl.aware;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class AwarenessLogFilter implements Filter {

	public boolean isLoggable(LogRecord record) {
		String loggerName = record.getLoggerName();
		String loggerMsg = record.getMessage();
		String loggerClass = record.getSourceClassName();
		// System.out.println("AwarenessLogFilter checking loggerName: " + loggerName);
		if (loggerName.equals(AwarenessHelpFuncs.class.getName())) {
			return true;
		}
		return false;
	}

}
