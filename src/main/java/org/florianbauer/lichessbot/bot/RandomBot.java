package org.florianbauer.lichessbot.bot;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import java.util.List;
import java.util.Random;

public class RandomBot extends Bot{
  private final Random random = new Random();

  public String selectMove(String fen) {
    Board board = new Board();
    board.loadFromFen(fen);

    List<Move> moves = board.legalMoves();

    if (!moves.isEmpty()) {
      Move randomMove = moves.get(random.nextInt(moves.size()));
      return randomMove.getSan();
    }
    else {
      // TODO handle empty legal moves list
    }

    return "";  // TODO remove placeholder
  }
}
