package com.thoughtworks.selenium.corebased;

import com.thoughtworks.selenium.InternalSelenseTestNgBase;

import org.testng.annotations.Test;

public class TestElementIndex extends InternalSelenseTestNgBase {
	@Test(dataProvider = "system-properties") public void testElementIndex() throws Exception {
		selenium.open("../tests/html/test_element_order.html");
		assertEquals(selenium.getElementIndex("d2"), "1");
		assertEquals(selenium.getElementIndex("d1.1.1"), "0");
		verifyEquals(selenium.getElementIndex("d2"), "1");
		verifyEquals(selenium.getElementIndex("d1.2"), "5");
		assertNotEquals("2", selenium.getElementIndex("d2"));
		verifyNotEquals("2", selenium.getElementIndex("d2"));
	}
}
