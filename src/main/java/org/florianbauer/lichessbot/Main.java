package org.florianbauer.lichessbot;

import org.florianbauer.lichessbot.handler.AccountHandler;

public class Main {

  public static void main(String[] args) {
    String token = System.getenv("LICHESS_TOKEN");
    try {
      new AccountHandler(token);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}