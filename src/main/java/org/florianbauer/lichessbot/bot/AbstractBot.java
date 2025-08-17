package org.florianbauer.lichessbot.bot;

import com.github.bhlangonijr.chesslib.move.Move;

public abstract class AbstractBot {
  private boolean isWhite;

  public AbstractBot(boolean isWhite) {
    this.isWhite = isWhite;
  }

  public abstract Move selectMove(String fen);
}
