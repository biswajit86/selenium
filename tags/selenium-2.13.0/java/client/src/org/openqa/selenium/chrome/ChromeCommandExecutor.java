package org.openqa.selenium.chrome;

import com.google.common.base.Throwables;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.Response;

import java.io.IOException;
import java.net.ConnectException;

/**
 * A specialized {@link HttpCommandExecutor} that will use a {@link ChromeDriverService} that lives
 * and dies with a single WebDriver session. The service will be restarted upon each new session
 * request and shutdown after each quit command.
 */
class ChromeCommandExecutor extends HttpCommandExecutor {

  private final ChromeDriverService service;

  /**
   * Creates a new ChromeCommandExecutor which will communicate with the chromedriver as configured
   * by the given {@code service}.
   * 
   * @param service The ChromeDriverService to send commands to.
   */
  public ChromeCommandExecutor(ChromeDriverService service) {
    super(service.getUrl());
    this.service = service;
  }

  /**
   * Sends the {@code command} to the chromedriver server for execution. The server will be started
   * if requesting a new session. Likewise, if terminating a session, the server will be shutdown
   * once a response is received.
   * 
   * @param command The command to execute.
   * @return The command response.
   * @throws IOException If an I/O error occurs while sending the command.
   */
  @Override
  public Response execute(Command command) throws IOException {
    if (DriverCommand.NEW_SESSION.equals(command.getName())) {
      service.start();
    }

    try {
      return super.execute(command);
    } catch (Throwable t) {
      Throwable rootCause = Throwables.getRootCause(t);
      if (rootCause instanceof ConnectException &&
          "Connection refused".equals(rootCause.getMessage()) &&
          !service.isRunning()) {
        throw new FatalChromeException(t);
      }
      Throwables.propagateIfPossible(t);
      throw new WebDriverException(t);
    } finally {
      if (DriverCommand.QUIT.equals(command.getName())) {
        service.stop();
      }
    }
  }
}
