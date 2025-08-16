package org.florianbauer.lichessbot.controller;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import org.florianbauer.lichessbot.api.LichessApi;

public class ChessGameController {

  private final LichessApi api;
  private final int maxGames;
  private final String userId;
  private final String username;
  private final ConcurrentHashMap<String, ChessGame> gamesList = new ConcurrentHashMap<>();
  private final ObjectMapper mapper = new ObjectMapper();

  public ChessGameController(String apiToken) throws IOException, InterruptedException {
    // default constructor with only one game
    this(apiToken, 1);
  }

  public ChessGameController(String apiToken, int maxGames)
      throws IOException, InterruptedException {
    this.api = new LichessApi(apiToken);
    this.maxGames = maxGames;

    JsonNode profile = mapper.readTree(this.api.getProfile());
    this.userId = profile.get("id").asText();
    this.username = profile.get("username").asText();
    if (!profile.has("title") || !profile.get("title").asText().equals("BOT")) {
      System.out.println("Account not a bot yet, upgrading to bot");
      api.upgradeToBot();
    } else {
      System.out.println("Already a bot account!");
    }

    startBot();
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
        case "gameStart" -> handleGameStartEvent(event);
        case "gameFinish" -> handleGameFinishEvent(event);
        case "challenge" -> handleChallengeEvent(event);
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

  private void handleGameStartEvent(JsonNode event) {
    String gameId = event.get("game").get("gameId").asText();
    String color = event.get("game").get("color").asText();
    boolean isWhite = color.equals("white");

    System.out.println("Game started: " + gameId);
    ChessGame game = new ChessGame(api, username, gameId, isWhite);
    gamesList.put(gameId, game);
    new Thread(game).start();
  }

  private void handleGameFinishEvent(JsonNode event) {
    String gameId = event.get("game").get("gameId").asText();

    System.out.println("Game finished: " + gameId);
    gamesList.remove(gameId);
  }

  private void handleChallengeEvent(JsonNode event) {
    String challengeId = event.get("challenge").get("id").asText();

    try {
      if (gamesList.size() < maxGames) {
        // accept challenge
        System.out.println("Challenge accepted: " + challengeId);
        api.acceptChallenge(challengeId);
      } else {
        // decline challenge
        System.out.println("Challenge declined: " + challengeId);
        api.declineChallenge(challengeId);
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }


}
