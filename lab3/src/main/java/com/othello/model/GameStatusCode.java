package com.othello.model;

public enum GameStatusCode {
    NONE,
    GAME_START,
    GAME_ALREADY_OVER,
    INVALID_COORDINATE,
    OUT_OF_BOUNDS,
    OCCUPIED,
    ILLEGAL_MOVE,
    PASS_NOT_ALLOWED,
    MOVE_ACCEPTED,
    PASS,
    GAME_OVER_FULL,
    GAME_OVER_NO_MOVES
}