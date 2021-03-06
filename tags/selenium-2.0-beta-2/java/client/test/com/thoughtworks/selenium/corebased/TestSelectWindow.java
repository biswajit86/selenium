package com.thoughtworks.selenium.corebased;

import java.util.regex.Pattern;

import com.thoughtworks.selenium.InternalSelenseTestNgBase;

import org.testng.annotations.Test;

public class TestSelectWindow extends InternalSelenseTestNgBase {
	@Test public void testSelectWindow() throws Exception {
		selenium.open("../tests/html/test_select_window.html");
		selenium.click("popupPage");
		selenium.waitForPopUp("myPopupWindow", "5000");
		selenium.selectWindow("name=myPopupWindow");
		verifyTrue(selenium.getLocation().matches("^[\\s\\S]*/tests/html/test_select_window_popup\\.html$"));
		verifyEquals(selenium.getTitle(), "Select Window Popup");
		verifyTrue(join(selenium.getAllWindowNames(), ',').matches("^[\\s\\S]*,[\\s\\S]*$"));
		verifyTrue(Pattern.compile("myPopupWindow").matcher(join(selenium.getAllWindowNames(), ',')).find());
		selenium.close();
		selenium.selectWindow("null");
		verifyTrue(selenium.getLocation().matches("^[\\s\\S]*/tests/html/test_select_window\\.html$"));
		selenium.click("popupPage");
		selenium.waitForPopUp("myPopupWindow", "5000");
		selenium.selectWindow("title=Select Window Popup");
		verifyTrue(selenium.getLocation().matches("^[\\s\\S]*/tests/html/test_select_window_popup\\.html$"));
		selenium.close();
		selenium.selectWindow("null");
		selenium.click("popupAnonymous");
		selenium.waitForPopUp("anonymouspopup", "5000");
		selenium.selectWindow("anonymouspopup");
		verifyTrue(selenium.getLocation().matches("^[\\s\\S]*/tests/html/test_select_window_popup\\.html$"));
		selenium.click("closePage");
		selenium.selectWindow("null");
		selenium.click("popupAnonymous");
		selenium.waitForPopUp("anonymouspopup", "5000");
		selenium.selectWindow("anonymouspopup");
		verifyTrue(selenium.getLocation().matches("^[\\s\\S]*/tests/html/test_select_window_popup\\.html$"));
		selenium.click("closePage2");
	}
}
