package org.florianbauer.lichessbot.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Random;
import org.florianbauer.lichessbot.api.LichessApi;

public class ChessGame implements Runnable {

  private final LichessApi api;
  private final String username;
  private final String gameId;
  private final boolean isWhite;
  private final ObjectMapper mapper = new ObjectMapper();
  private final Random random = new Random();  // TODO: for random-moves MVP, will be replaced by bot later

  public ChessGame(LichessApi api, String username, String gameId, boolean isWhite) {
    this.api = api;
    this.username = username;
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
        case "gameFull" -> handleGameFullEvent(event);
        case "gameState" -> handleGameStateEvent(event);
        case "chatLine" -> handleChatLineEvent(event);
        case "opponentGone" -> handleOpponentGoneEvent(event);
        default -> {
          // ignore
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void handleGameFullEvent(JsonNode event) {

  }

  private void handleGameStateEvent(JsonNode event) {

  }

  private void handleChatLineEvent(JsonNode event) {
    String messageFromUsername = event.get("username").asText();

    if (!messageFromUsername.equals(username)) {
      try {
        api.writeChatMessage(gameId, "Howdy, I'm a bot!");
        // TODO write nicer answers, maybe include LLM?
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }

  private void handleOpponentGoneEvent(JsonNode event) {
    // TODO implement
  }
}
