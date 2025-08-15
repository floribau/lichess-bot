package org.florianbauer.lichessbot.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Random;
import org.florianbauer.lichessbot.api.LichessApi;

public class ChessGame implements Runnable {

  private final LichessApi api;
  private final String gameId;
  private final boolean isWhite;
  private final ObjectMapper mapper = new ObjectMapper();
  private final Random random = new Random();

  public ChessGame(LichessApi api, String gameId, boolean isWhite) {
    this.api = api;
    this.gameId = gameId;
    this.isWhite = isWhite;
  }

  @Override
  public void run() {
    try {
      api.streamGameState(gameId, json -> handleGameState(json));
      // TODO throw error if code 404
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void handleGameState(String json) {
    try {
      JsonNode event = mapper.readTree(json);
      String type = event.get("type").asText();
      switch (type) {
        case "gameFull" -> System.out.println();
        case "gameState" -> System.out.println();
        case "chatLine" -> System.out.println();
        case "opponentGone" -> System.out.println();
        default -> {
          // ignore
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
