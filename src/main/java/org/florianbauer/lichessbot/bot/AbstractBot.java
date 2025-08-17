package org.florianbauer.lichessbot.bot;

import com.github.bhlangonijr.chesslib.move.Move;

public abstract class AbstractBot {

  public abstract Move selectMove(String fen);
}
