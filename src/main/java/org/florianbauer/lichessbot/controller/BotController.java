package org.florianbauer.lichessbot.controller;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import org.florianbauer.lichessbot.api.LichessApi;

public class BotController {

  private final LichessApi api;
  private final ObjectMapper mapper = new ObjectMapper();
  private final Random random = new Random();
  private final AtomicBoolean inGame = new AtomicBoolean(false);  // TODO: maybe use synchronized string with gameId instead

  public BotController(String apiToken){
    this.api = new LichessApi(apiToken);
  }

  public void start() throws IOException, InterruptedException {
    api.streamEvents(this::handleEvent);
  }

  private void handleEvent(String json) {
    try {
      JsonNode event = mapper.readTree(json);
      String type = event.get("type").asText();
      switch (type) {
        case "gameStart" -> System.out.println();
        case "gameFinish" -> System.out.println();
        case "challenge" -> System.out.println();
        case "challengeCanceled" -> System.out.println();
        case "challengeDeclined" -> System.out.println();
        default -> {break;}
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void handleGameStart(JsonNode event) {
    String gameId = "";  // TODO get gameId from event
    inGame.set(true);

    new Thread(() -> {
      try {
        System.out.println("Game thread started: " + gameId);
        api.streamGameState(gameId, json -> handleGameState(gameId, json));
      } catch(Exception e) {
        e.printStackTrace();
      } finally {
        inGame.set(false);
        System.out.println("Game thread ended: " + gameId);
      }
    }).start();
  }

  private void handleGameState(String gameId, String json) {

  }
}
