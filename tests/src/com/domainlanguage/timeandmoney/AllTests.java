/**
 * Copyright (c) 2004 Domain Language, Inc. (http://domainlanguage.com)
 * This free software is distributed under the "MIT" licence. See file licence.txt. 
 * For more information, see http://timeandmoney.sourceforge.net.
 */

package com.domainlanguage.timeandmoney;

import com.domainlanguage.basic.ComparableIntervalTest;
import junit.framework.*;

public class AllTests extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new TestSuite(ComparableIntervalTest.class));
		suite.addTest(com.domainlanguage.time.AllTests.suite());
		suite.addTest(com.domainlanguage.money.AllTests.suite());

		return suite;
	}

}