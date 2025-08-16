package org.florianbauer.lichessbot.exception;

public class LichessException extends Exception{
  public LichessException() {
    super();
  }

  public LichessException(String message) {
    super(message);
  }

  public LichessException(String message, Throwable cause) {
    super(message, cause);
  }

  public LichessException(Throwable cause) {
    super(cause);
  }
}
