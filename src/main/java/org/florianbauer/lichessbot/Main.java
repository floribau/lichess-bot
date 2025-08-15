package org.florianbauer.lichessbot;

import java.io.IOException;
import org.florianbauer.lichessbot.controller.ChessGameController;

public class Main {

  public static void main(String[] args) {
    System.out.println("Hello world!");
    String token = System.getenv("LICHESS_TOKEN");
    try {
      ChessGameController controller = new ChessGameController(token);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}