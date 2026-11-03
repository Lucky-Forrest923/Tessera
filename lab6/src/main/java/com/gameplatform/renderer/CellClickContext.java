package com.gameplatform.renderer;

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

public class CellClickContext {
    private final Runnable refreshAll;
    private final Runnable refreshBoard;
    private final Runnable refreshStatus;
    private final BiConsumer<String, Boolean> setStatus;
    private final BooleanSupplier flagMode;
    private final CellClickHandler clickHandler;

    public interface CellClickHandler {
        void onClick(int row, int col, boolean isSecondary, boolean isFlagMode);
    }

    public CellClickContext(Runnable refreshAll, Runnable refreshBoard, Runnable refreshStatus,
                            BiConsumer<String, Boolean> setStatus, BooleanSupplier flagMode,
                            CellClickHandler clickHandler) {
        this.refreshAll = refreshAll;
        this.refreshBoard = refreshBoard;
        this.refreshStatus = refreshStatus;
        this.setStatus = setStatus;
        this.flagMode = flagMode;
        this.clickHandler = clickHandler;
    }

    public void refreshAll() { refreshAll.run(); }
    public void refreshBoard() { refreshBoard.run(); }
    public void refreshStatus() { refreshStatus.run(); }
    public void setStatus(String msg, Boolean err) { setStatus.accept(msg, err); }
    public boolean isFlagMode() { return flagMode.getAsBoolean(); }
    public void handleCellClick(int row, int col, boolean isSecondary) {
        clickHandler.onClick(row, col, isSecondary, isFlagMode());
    }
}
