/*
Copyright 2011 WebDriver committers
Copyright 2011 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.openqa.selenium.html5;

import static org.openqa.selenium.TestWaiter.waitFor;

import org.openqa.selenium.AbstractDriverTestCase;
import org.openqa.selenium.WaitingConditions;

import java.util.Map;

public class SqlDatabaseTest extends AbstractDriverTestCase {
  private static final String INSERT_STATEMENT =
      "INSERT INTO docs(docname) VALUES (?)";
  private static final String SELECT_STATEMENT = "SELECT * FROM docs";
  private static final String DELETE_STATEMENT = "DELETE FROM docs";

  private ResultSet executeQuery(String statement, String... param) {
    // Note: Current HTML5 API of Webdriver only requires the databaseName.
    // The version, displayName and initial quota size is assigned default value.
    // Default value of version is empty string, which is fine since in this case,
    // there is no expected version â€” any version is fine.
    // See: http://www.w3.org/TR/webdatabase/#dom-opendatabase
    // Default value of the initial quota is 5MB.
    String databaseName = "HTML5";
    return ((DatabaseStorage) driver).executeSQL(databaseName, statement,
        (Object[]) param);
  }

  public void testResultSetsReturnNegativeLastInsertedRowId() {
    if (!(driver instanceof DatabaseStorage)) {
      return;
    }
    driver.get(pages.html5Page);
    waitFor(WaitingConditions.elementToExist(driver, "db_completed"));

    ResultSet resultSet = executeQuery(SELECT_STATEMENT);
    assertTrue(resultSet.getLastInsertedRowId() == -1);
  }

  public void testResultSetsReturnPositiveLastInsertedRowId() {
    if (!(driver instanceof DatabaseStorage)) {
      return;
    }
    driver.get(pages.html5Page);
    waitFor(WaitingConditions.elementToExist(driver, "db_completed"));

    ResultSet resultSet = executeQuery(INSERT_STATEMENT, "DocFoo");
    assertTrue(resultSet.getLastInsertedRowId() != -1);

    ResultSet resultSet1 = executeQuery(SELECT_STATEMENT);
    assertTrue(resultSet1.getLastInsertedRowId() == -1);

    ResultSet resultSet2 = executeQuery(DELETE_STATEMENT);
    assertTrue(resultSet2.getLastInsertedRowId() == -1);
  }

  public void testResultSetsNumberOfRowsAffected() {
    if (!(driver instanceof DatabaseStorage)) {
      return;
    }
    driver.get(pages.html5Page);
    waitFor(WaitingConditions.elementToExist(driver, "db_completed"));

    ResultSet resultSet = executeQuery(INSERT_STATEMENT, "DocFooBar");
    assertTrue(resultSet.getNumberOfRowsAffected() == 1);

    ResultSet resultSet1 = executeQuery(SELECT_STATEMENT);
    assertTrue(resultSet1.getNumberOfRowsAffected() == 0);

    ResultSet resultSet2 = executeQuery(
        "UPDATE docs SET docname='DocBar' WHERE docname='DocFooBar'");
    assertTrue(
        "It should only affect one row, but affects " + resultSet2.getNumberOfRowsAffected(),
        resultSet2.getNumberOfRowsAffected() == 1);

    executeQuery(DELETE_STATEMENT);
  }

  public void testResultSetRowsContainsInsertedRows() {
    if (!(driver instanceof DatabaseStorage)) {
      return;
    }
    driver.get(pages.html5Page);
    waitFor(WaitingConditions.elementToExist(driver, "db_completed"));

    executeQuery(INSERT_STATEMENT, "DocFoo");
    executeQuery(INSERT_STATEMENT, "DocFooBar");
    ResultSet resultSet = executeQuery(SELECT_STATEMENT);
    assertTrue(resultSet.rows().size() == 2);

    Map<String, Object> record = resultSet.rows().item(0);
    assertEquals("DocFoo", record.get("docname"));
    record = resultSet.rows().item(1);
    assertEquals("DocFooBar", record.get("docname"));

    executeQuery(DELETE_STATEMENT);
    resultSet = executeQuery(SELECT_STATEMENT);
    assertTrue(resultSet.rows().size() == 0);
  }
}
