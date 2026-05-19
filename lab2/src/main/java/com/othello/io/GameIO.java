package com.othello.io;

/**
 * 游戏前端实现的输入/输出接口。
 */
public interface GameIO extends AutoCloseable {
    /**
     * 绘制设置界面。
     * @param input 当前输入
     * @param message 提示信息
     * @throws Exception 绘制异常
     */
    void drawSetupScreen(String input, String message) throws Exception;

    /**
     * 绘制对局界面。
     * @param viewData 控制器提供的渲染数据
     * @throws Exception 绘制异常
     */
    void drawGame(GameViewData viewData) throws Exception;

    /**
     * 读取用户输入。
     * @return 输入按键信息
     * @throws Exception 读取异常
     */
    InputKey readInput() throws Exception;

    /**
     * 关闭输入/输出资源。
     * @throws Exception 关闭异常
     */
    @Override
    void close() throws Exception;
}
