package engine.ai;

import engine.board.Board;
import engine.board.Move;
import engine.board.MoveTransition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MiniMax {

    private final BoardEvaluator evaluator;

    public MiniMax(final BoardEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public List<MoveScore> getBestMoves(final Board board, final int depth, final int maxMoves) {
        if (depth <= 0) {
            return List.of();
        }
        final boolean maximizing = board.getCurrentPlayer().getAlliance().isWhite();
        final int perspective = maximizing ? 1 : -1;
        final List<MoveScore> scoredMoves = new ArrayList<>();
        final List<Move> legalMoves = new ArrayList<>(board.getCurrentPlayer().getLegalMoves());
        if (legalMoves.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.shuffle(legalMoves, ThreadLocalRandom.current());
        for (final Move move : legalMoves) {
            final MoveTransition transition = board.getCurrentPlayer().makeMove(move);
            if (!transition.getMoveStatus().isDone()) {
                continue;
            }
            final int score = minimax(transition.getToBoard(), depth - 1,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, !maximizing);
            scoredMoves.add(new MoveScore(move, perspective * score));
        }
        scoredMoves.sort((a, b) -> Integer.compare(b.score(), a.score()));
        if (maxMoves > 0 && scoredMoves.size() > maxMoves) {
            return List.copyOf(scoredMoves.subList(0, maxMoves));
        }
        return Collections.unmodifiableList(scoredMoves);
    }

    private int minimax(final Board board,
                        final int depth,
                        int alpha,
                        int beta,
                        final boolean maximizingPlayer) {
        if (depth == 0 || board.getCurrentPlayer().isInCheckMate() || board.getCurrentPlayer().isInStaleMate()) {
            return evaluator.evaluate(board, depth);
        }

        int bestValue = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        boolean moveMade = false;

        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition transition = board.getCurrentPlayer().makeMove(move);
            if (!transition.getMoveStatus().isDone()) {
                continue;
            }
            moveMade = true;
            final int value = minimax(transition.getToBoard(), depth - 1, alpha, beta, !maximizingPlayer);
            if (maximizingPlayer) {
                bestValue = Math.max(bestValue, value);
                alpha = Math.max(alpha, value);
            } else {
                bestValue = Math.min(bestValue, value);
                beta = Math.min(beta, value);
            }
            if (beta <= alpha) {
                break;
            }
        }

        if (!moveMade) {
            return evaluator.evaluate(board, depth);
        }

        return bestValue;
    }
}
