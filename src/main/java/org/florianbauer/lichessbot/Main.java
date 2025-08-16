package org.florianbauer.lichessbot;

import java.io.IOException;
import org.florianbauer.lichessbot.controller.ChessGameController;

public class Main {

  public static void main(String[] args) {
    String token = System.getenv("LICHESS_TOKEN");
    try {
      new ChessGameController(token);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}