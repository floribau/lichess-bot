package org.florianbauer.lichessbot.bot;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import java.util.List;
import java.util.Random;

public class RandomBot extends AbstractBot {
  private final Random random = new Random();

  public RandomBot(boolean isWhite) {
    super(isWhite);
  }

  @Override
  public Move selectMove(String fen) {
    Board board = new Board();
    board.loadFromFen(fen);

    List<Move> moves = board.legalMoves();

    if (moves.isEmpty()) {
      // TODO handle empty legal moves list
    }

    return moves.get(random.nextInt(moves.size()));
  }
}
