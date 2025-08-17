package org.florianbauer.lichessbot.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bhlangonijr.chesslib.move.MoveList;
import java.io.IOException;
import org.florianbauer.lichessbot.api.LichessApi;
import org.florianbauer.lichessbot.bot.AbstractBot;
import org.florianbauer.lichessbot.exception.LichessException;

public class ChessGameHandler implements Runnable {

  private final LichessApi api;
  private final AbstractBot bot;
  private final String username;
  private final String gameId;
  private final boolean isWhite;
  private final ObjectMapper mapper = new ObjectMapper();

  public ChessGameHandler(LichessApi api, AbstractBot bot, String username, String gameId, boolean isWhite) {
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

  private boolean isMyTurn(String san) {
    String[] moves = san.trim().split("\\s+");
    int moveCount = san.isBlank() ? 0 : moves.length;
    return (isWhite && moveCount % 2 == 0) || (!isWhite && moveCount % 2 == 1);
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
    String san = event.get("state").get("moves").asText();

    if (isMyTurn(san)) {
      String initialFen = event.get("initialFen").asText();
      if (initialFen.equals("startpos")) {
        initialFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
      }

      String move = bot.selectMove(initialFen);
      api.makeMove(gameId, move);
    }
  }

  private void handleGameStateEvent(JsonNode event)
      throws LichessException, IOException, InterruptedException {
    String san = event.get("moves").asText();

    if (isMyTurn(san)) {
      MoveList moveList = new MoveList();
      moveList.loadFromSan(san);
      String fen = moveList.getFen();

      String move = bot.selectMove(fen);
      api.makeMove(gameId, move);
    }
  }

  private void handleChatLineEvent(JsonNode event) {
    String fromUsername = event.get("username").asText();
    String message = event.get("text").asText();
    String room = event.get("room").asText();
    System.out.println("New chat message from " + fromUsername + ": " + message);

    if (!fromUsername.equals(username)) {
      try {
        String answerMessage = "Howdy, I'm a bot!";
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
