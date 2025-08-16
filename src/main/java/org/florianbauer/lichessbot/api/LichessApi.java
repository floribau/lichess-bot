package org.florianbauer.lichessbot.api;

import java.net.http.*;
import java.net.URI;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.florianbauer.lichessbot.exception.LichessException;

public class LichessApi {

  private static final String BASE_URL = "https://lichess.org";
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

  // get my profile
  public String getProfile() throws IOException, InterruptedException, LichessException {
    HttpRequest request = authorizedRequest("/api/account").GET().build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      // TODO case differentiation based on status code
      throw new LichessException(
          "getProfile request failed: HTTP " + response.statusCode() + " - " + response.body()
      );
    }

    return response.body();
  }

  // accept challenge
  public void acceptChallenge(String challengeId) throws IOException, InterruptedException, LichessException {
    HttpRequest request = authorizedRequest("/api/challenge/" + challengeId + "/accept")
        .POST(HttpRequest.BodyPublishers.noBody())
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new LichessException(
          "acceptChallenge request failed: HTTP " + response.statusCode() + " - " + response.body()
      );
    }
  }

  // decline challenge
  public void declineChallenge(String challengeId) throws IOException, InterruptedException, LichessException {
    HttpRequest request = authorizedRequest("/api/challenge/" + challengeId + "/decline")
        .POST(HttpRequest.BodyPublishers.noBody())
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new LichessException(
          "declineChallenge request failed: HTTP " + response.statusCode() + " - " + response.body()
      );
    }
  }

  // stream incoming events
  public void streamEvents(Consumer<String> listener) throws IOException, InterruptedException, LichessException {
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
  public void upgradeToBot() throws IOException, InterruptedException, LichessException {
    HttpRequest request = authorizedRequest("/api/bot/account/upgrade")
        .POST(HttpRequest.BodyPublishers.noBody())
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    // TODO verify if upgrade will fail if account is already a bot

    if (response.statusCode() != 200) {
      throw new LichessException(
          "upgradeBot request failed: HTTP " + response.statusCode() + " - " + response.body()
      );
    }
  }

  // stream bot game state
  public void streamGameState(String gameId, Consumer<String> listener) throws IOException, InterruptedException, LichessException, LichessException {
    HttpRequest request = authorizedRequest("/api/bot/game/stream/" + gameId).GET().build();
    HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

    if (response.statusCode() != 200) {
      throw new LichessException(
          "streamGameState request failed: HTTP " + response.statusCode() + " - game " + gameId + " was not found"
          // TODO maybe use actual message body instead
      );
    }

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
  public void makeMove(String gameId, String uciMove) throws IOException, InterruptedException, LichessException {
    HttpRequest request = authorizedRequest("/api/bot/game/" + gameId + "/move/" + uciMove)
        .POST(HttpRequest.BodyPublishers.noBody())
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new LichessException(
          "makeMove request failed: HTTP " + response.statusCode() + " - " + response.body()
      );
    }
  }

  public void writeChatMessage(String gameId, String message, String room)
      throws IOException, InterruptedException, LichessException {
    String requestBody = String.format("{\"room\": \"%s\", \"text\": \"%s\"}", room, message);
    HttpRequest request = authorizedRequest("/api/bot/game/" + gameId + "/chat")
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .header("Content-Type", "application/json")
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new LichessException(
          "writeChatMessage request failed: HTTP " + response.statusCode() + " - " + response.body()
      );
    }
  }

  // handle draw offers
  public void handleDrawOffer(String gameId, boolean acceptDraw) throws IOException, InterruptedException, LichessException {
    String accept = acceptDraw ? "yes" : "no";
    HttpRequest request = authorizedRequest("/api/bot/game/" + gameId + "/draw/" + accept)
        .POST(HttpRequest.BodyPublishers.noBody())
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new LichessException(
          "handleDrawOffer request failed: HTTP " + response.statusCode() + " - " + response.body()
      );
    }
  }

  // handle takeback offers
  public void handleTakebackOffer(String gameId, boolean acceptTakeback) throws IOException, InterruptedException, LichessException {
    String accept = acceptTakeback ? "yes" : "no";
    HttpRequest request = authorizedRequest("/api/bot/game/" + gameId + "/takeback/" + accept)
        .POST(HttpRequest.BodyPublishers.noBody())
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new LichessException(
          "handleTakebackOffer request failed: HTTP " + response.statusCode() + " - " + response.body()
      );
    }
  }

}
