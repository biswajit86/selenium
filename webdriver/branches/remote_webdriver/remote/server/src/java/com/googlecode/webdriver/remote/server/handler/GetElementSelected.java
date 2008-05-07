package com.googlecode.webdriver.remote.server.handler;

import com.googlecode.webdriver.remote.Response;
import com.googlecode.webdriver.remote.server.DriverSessions;
import com.googlecode.webdriver.remote.server.rest.ResultType;

public class GetElementSelected extends WebDriverHandler {
    private String elementId;
    private Response response;

    public GetElementSelected(DriverSessions sessions) {
        super(sessions);
    }

    public void setId(String elementId) {
        this.elementId = elementId;
    }

    public ResultType handle() throws Exception {
        response = newResponse();
        response.setValue(getKnownElements().get(elementId).isSelected());

        return ResultType.SUCCESS;
    }

    public Response getResponse() {
        return response;
    }
}
