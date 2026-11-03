package com.gameplatform.model;

import java.util.List;

public interface HintProvider {
    boolean showLegalMovesAsHints();
    List<Position> getLegalMoves();
}
