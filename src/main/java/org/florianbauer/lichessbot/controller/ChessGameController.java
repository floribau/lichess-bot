package org.florianbauer.lichessbot.controller;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import org.florianbauer.lichessbot.api.LichessApi;

public class ChessGameController {

  private final LichessApi api;
  private final int maxGames;
  private final ConcurrentHashMap<String, ChessGame> gamesList = new ConcurrentHashMap<>();
  private final ObjectMapper mapper = new ObjectMapper();

  public ChessGameController(String apiToken){
    // default constructor with only one game
    this(apiToken, 1);
  }

  public ChessGameController(String apiToken, int maxGames) {
    this.api = new LichessApi(apiToken);
    this.maxGames = maxGames;
  }

  public void startBot() throws IOException, InterruptedException {
    api.streamEvents(this::handleEvent);
    // TODO throw error if code 404
  }

  private void handleEvent(String json) {
    try {
      JsonNode event = mapper.readTree(json);
      String type = event.get("type").asText();
      switch (type) {
        case "gameStart" -> handleGameStart(event);
        case "gameFinish" -> handleGameFinish(event);
        case "challenge" -> handleChallenge(event);
        // case "challengeCanceled" -> System.out.println();
        // case "challengeDeclined" -> System.out.println();
        default -> {
          // ignore
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void handleGameStart(JsonNode event) {
    String gameId = event.get("game").get("gameId").asText();
    String color = event.get("game").get("color").asText();
    boolean isWhite = color.equals("white");

    System.out.println("Game started: " + gameId);
    ChessGame game = new ChessGame(api, gameId, isWhite);
    gamesList.put(gameId, game);
    new Thread(game).start();
  }

  private void handleGameFinish(JsonNode event) {
    String gameId = event.get("game").get("gameId").asText();

    System.out.println("Game finished: " + gameId);
    gamesList.remove(gameId);
  }

  private void handleChallenge(JsonNode event) {
    String challengeId = event.get("challenge").get("id").asText();

    try {
      if (gamesList.size() < maxGames) {
        // accept challenge
        api.acceptChallenge(challengeId);
      } else {
        // decline challenge
        api.declineChallenge(challengeId);
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }


}
