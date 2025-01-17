package org.contamination;

import com.google.gson.Gson;
import java.io.IOException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import static org.contamination.GameState.PLAYER_INPUTS;
import static org.contamination.GameState.SESSION_IDS_PLAYERS;
import static org.contamination.GameState.addPlayer;
import static org.contamination.GameState.addSpectator;
import static org.contamination.GameState.removePlayer;
import static org.contamination.PlayerStatus.READY;

@ServerEndpoint(value = "/websocket/{username}")
public class GameWebsocket {

  @OnOpen
  public void onOpen(Session session, @PathParam("username") String username) throws IOException {
    Player player = new Player(username);
    if (GameState.GAME_STATUS == GameStatus.PENDING) {
      addPlayer(player, session);
    } else {
      addSpectator(player, session);
    }
    session.getBasicRemote().sendText(new Gson().toJson(new ReplyMessage("CONNECTED", new ConnectedMessage(player.getId()))));
  }


  @OnMessage
  public void onMessage(Session session, String message) {

    Message msg = new Gson().fromJson(message, Message.class);
    Player player = SESSION_IDS_PLAYERS.get(session.getId());
    switch (msg.type()) {
      case "ready" -> player.setStatus(READY);
      case "add_bot" -> GameState.addBot();
      case "remove_bot" -> GameState.removeBot();
      case "start" -> GameState.start();
      case "commands" -> handlePlayerInputChange(player, msg);
    }

  }

  private void handlePlayerInputChange(Player player, Message msg) {
    PlayerInputMessage playerInputChange = new Gson().fromJson(msg.content(), PlayerInputMessage.class);
    PlayerInput playerInput = PLAYER_INPUTS.get(player.getId());
    if (playerInputChange.down() != null) {
      playerInput.down = playerInputChange.down();
    }
    if (playerInputChange.up() != null) {
      playerInput.up = playerInputChange.up();
    }
    if (playerInputChange.right() != null) {
      playerInput.right = playerInputChange.right();
    }
    if (playerInputChange.left() != null) {
      playerInput.left = playerInputChange.left();
    }
  }

  @OnClose
  public void onClose(Session session) throws IOException {
    removePlayer(session);
  }

  @OnError
  public void onError(Session session, Throwable throwable) {
    throwable.printStackTrace();
  }
}
