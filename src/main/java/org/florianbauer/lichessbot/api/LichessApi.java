package org.florianbauer.lichessbot.api;

import java.net.http.*;
import java.net.URI;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class LichessApi {

  private static final String BASE_URL = "http://lichess.org";
  private final String token;
  private final HttpClient client;

  public LichessApi(String token) {
    this.token = token;
    this.client = HttpClient.newHttpClient();
  }

  private HttpRequest.Builder authorizedRequest(String endpoint) {
    return HttpRequest.newBuilder()
        .uri(URI.create(BASE_URL + endpoint))
        .header("Authorization", "Bearer " + token);
  }

  // list challenges
  public void getChallenges() throws IOException, InterruptedException {
    HttpRequest request = authorizedRequest("/api/challenge").GET().build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  // accept challenge
  public boolean acceptChallenge(String challengId) throws IOException, InterruptedException {
    HttpRequest request = authorizedRequest("/api/challenge/" + challengId + "/accept")
        .POST(HttpRequest.BodyPublishers.noBody())
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    return response.statusCode() == 200;
  }

  // decline challenge
  public boolean declineChallenge(String challengId) throws IOException, InterruptedException {
    HttpRequest request = authorizedRequest("/api/challenge/" + challengId + "/decline")
        .POST(HttpRequest.BodyPublishers.noBody())
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    return response.statusCode() == 200;
  }

  // stream incoming events
  public void streamEvents(Consumer<String> listener) throws IOException, InterruptedException {
    HttpRequest request = authorizedRequest("/api/stream/event").GET().build();
    HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(response.body(), StandardCharsets.UTF_8)
    )) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (!line.isBlank()) {
          listener.accept(line);
        }
      }
    }
  }

  // upgrade to bot account
  public boolean upgradeBot() throws IOException, InterruptedException {
    HttpRequest request = authorizedRequest("/api/bot/account/upgrade")
        .POST(HttpRequest.BodyPublishers.noBody())
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    // TODO: check if upgrade will fail if account is already a bot
    return response.statusCode() == 200;
  }

  // stream bot game state
  public void streamGameState(String gameId, Consumer<String> listener) throws IOException, InterruptedException {
    HttpRequest request = authorizedRequest("/api/bot/game/stream/" + gameId).GET().build();
    HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(response.body(), StandardCharsets.UTF_8)
    )) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (!line.isBlank()) {
          listener.accept(line);
        }
      }
    }
  }

  // make bot move
  public boolean makeMove(String gameId, String uciMove) throws IOException, InterruptedException {
    HttpRequest request = authorizedRequest("/api/bot/game/" + gameId + "/move/" + uciMove)
        .POST(HttpRequest.BodyPublishers.noBody())
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    return response.statusCode() == 200;
  }

  // handle draw offers
  public boolean handleDrawOffer(String gameId, boolean acceptDraw) throws IOException, InterruptedException {
    String accept = acceptDraw ? "yes" : "no";
    HttpRequest request = authorizedRequest("/api/bot/game/" + gameId + "/draw/" + accept)
        .POST(HttpRequest.BodyPublishers.noBody())
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    return response.statusCode() == 200;
  }

  // handle takeback offers
  public boolean handleTakebackOffer(String gameId, boolean acceptTakeback) throws IOException, InterruptedException {
    String accept = acceptTakeback ? "yes" : "no";
    HttpRequest request = authorizedRequest("/api/bot/game/" + gameId + "/takeback/" + accept)
        .POST(HttpRequest.BodyPublishers.noBody())
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    return response.statusCode() == 200;
  }

}
