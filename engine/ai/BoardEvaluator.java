package engine.ai;

import engine.board.Board;

public interface BoardEvaluator {

    int evaluate(Board board, int depth);
}
