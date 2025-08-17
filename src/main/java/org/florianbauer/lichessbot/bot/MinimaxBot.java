package org.florianbauer.lichessbot.bot;

import com.github.bhlangonijr.chesslib.move.Move;

public class MinimaxBot extends AbstractBot {
  private boolean isWhite;

  public MinimaxBot(boolean isWhite) {
    this.isWhite = isWhite;
  }

  @Override
  public Move selectMove(String fen) {
    // TODO implement
    return null;
  }
}
