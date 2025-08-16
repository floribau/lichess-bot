package org.florianbauer.lichessbot.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bhlangonijr.chesslib.move.MoveList;
import java.io.IOException;
import org.florianbauer.lichessbot.api.LichessApi;
import org.florianbauer.lichessbot.bot.AbstractBot;
import org.florianbauer.lichessbot.exception.LichessException;

public class ChessGame implements Runnable {

  private final LichessApi api;
  private final AbstractBot bot;
  private final String username;
  private final String gameId;
  private final boolean isWhite;
  private final ObjectMapper mapper = new ObjectMapper();

  public ChessGame(LichessApi api, AbstractBot bot, String username, String gameId, boolean isWhite) {
    this.api = api;
    this.bot = bot;
    this.username = username;
    this.gameId = gameId;
    this.isWhite = isWhite;
  }

  @Override
  public void run() {
    try {
      api.streamGameState(gameId, json -> handleGameState(json));
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

  private void handleGameFullEvent(JsonNode event)
      throws LichessException, IOException, InterruptedException {
    String san = event.get("moves").asText();
    MoveList list = new MoveList();
    list.loadFromSan(san);
    System.out.println("gameFullEvent, moves: " + san);

    if (isWhite) {
      api.makeMove(gameId, bot.selectMove(list.getFen()));
    }
  }

  private void handleGameStateEvent(JsonNode event) {
    String san = event.get("moves").asText();
    System.out.println("gameStateEvent, moves: " + san);
  }

  private void handleChatLineEvent(JsonNode event) {
    String fromUsername = event.get("username").asText();
    String message = event.get("text").asText();
    String room = event.get("room").asText();
    System.out.println("New chat message from " + fromUsername + ": " + message);

    if (!fromUsername.equals(username)) {
      try {
        String answerMessage = "Howdy, I'm a abstractBot!";
        // TODO write nicer answers depending on message(s), maybe include LLM?
        api.writeChatMessage(gameId, answerMessage, room);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }

  private void handleOpponentGoneEvent(JsonNode event) {
    // TODO implement
  }
}
