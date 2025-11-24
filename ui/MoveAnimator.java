package ui;

import engine.pieces.Piece;
import javafx.animation.AnimationTimer;

final class MoveAnimator {

    private final long durationNanos;
    private final Runnable onFrame;
    private final Runnable onComplete;
    private final AnimationTimer timer;

    private boolean animating;
    private long startNanos;
    private double progress;
    private Piece piece;
    private int destinationCoordinate = -1;
    private double startX;
    private double startY;
    private double endX;
    private double endY;

    MoveAnimator(final long durationNanos,
                 final Runnable onFrame,
                 final Runnable onComplete) {
        this.durationNanos = durationNanos;
        this.onFrame = onFrame;
        this.onComplete = onComplete;
        this.timer = new AnimationTimer() {
            @Override
            public void handle(final long now) {
                if (!animating) {
                    return;
                }
                final long elapsed = now - startNanos;
                progress = Math.min(1.0, elapsed / (double) durationNanos);
                onFrame.run();
                if (progress >= 1.0) {
                    stopInternal();
                    onComplete.run();
                }
            }
        };
    }

    void start(final Piece piece,
               final int destinationCoordinate,
               final double startX,
               final double startY,
               final double endX,
               final double endY) {
        cancel();
        this.piece = piece;
        this.destinationCoordinate = destinationCoordinate;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.progress = 0.0;
        this.startNanos = System.nanoTime();
        this.animating = true;
        timer.start();
        onFrame.run();
    }

    void cancel() {
        if (animating) {
            stopInternal();
        }
    }

    void finishImmediately() {
        if (animating) {
            stopInternal();
            onFrame.run();
        }
        onComplete.run();
    }

    boolean isAnimating() {
        return animating;
    }

    double getProgress() {
        return progress;
    }

    Piece getPiece() {
        return piece;
    }

    int getDestinationCoordinate() {
        return destinationCoordinate;
    }

    double getStartX() {
        return startX;
    }

    double getStartY() {
        return startY;
    }

    double getEndX() {
        return endX;
    }

    double getEndY() {
        return endY;
    }

    private void stopInternal() {
        timer.stop();
        animating = false;
        progress = 1.0;
        piece = null;
        destinationCoordinate = -1;
    }
}

