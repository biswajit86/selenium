package org.openqa.grid.internal.mock;

import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.TestSession;
import org.openqa.grid.web.servlet.handler.RequestHandler;
import org.openqa.grid.web.servlet.handler.RequestType;
import org.openqa.grid.web.servlet.handler.WebDriverRequestHandler;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MockedRequestHandler extends WebDriverRequestHandler {

  public MockedRequestHandler(Registry registry) {
    super(null, null, registry);
  }

  public MockedRequestHandler(HttpServletRequest request, HttpServletResponse response,
      Registry registry) {
    super(request, response, registry);
  }

  @Override
  public String forwardNewSessionRequest(TestSession session) {
    // System.out.println("forwarding to " + session.getInternalKey());
    return "";
  }

  @Override
  protected void forwardRequest(TestSession session, RequestHandler handler)
      throws java.io.IOException {
    // System.out.println("forwarding request to "+session);
  }

  ;


  @Override
  public void setSession(TestSession session) {
    super.setSession(session);
  }

  public TestSession getTestSession() {
    return super.getSession();
  }

  @Override
  public void setDesiredCapabilities(Map<String, Object> desiredCapabilities) {
    super.setDesiredCapabilities(desiredCapabilities);
  }

  @Override
  public void setRequestType(RequestType requestType) {
    super.setRequestType(requestType);
  }
}
