/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freckler.sight.impl.aware;

import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class AwarenessLogFormatter extends Formatter {

	private static final MessageFormat messageFormat = new MessageFormat("{0,date,h:mm:ss} {2} {1}: {3} \n");

	public AwarenessLogFormatter() {
		super();
	}

	@Override public String format(LogRecord record) {
		Object[] arguments = new Object[5];
		arguments[0] = new Date(record.getMillis());
		arguments[1] = record.getSourceMethodName();
		arguments[2] = record.getLevel();
		arguments[3] = record.getMessage();
		return messageFormat.format(arguments);
	}

}
