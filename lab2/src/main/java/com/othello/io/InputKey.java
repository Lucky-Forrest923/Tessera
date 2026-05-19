package com.othello.io;

/**
 * 轻量级按键事件，与具体终端库无关。
 */
public final class InputKey {
    private final InputKeyType type;
    private final Character character;

    /**
     * 构造按键对象。
     * @param type 按键类型
     * @param character 字符内容（仅字符型按键有效）
     */
    public InputKey(InputKeyType type, Character character) {
        this.type = type;
        this.character = character;
    }

    /**
     * 获取按键类型。
     * @return 按键类型
     */
    public InputKeyType type() {
        return type;
    }

    /**
     * 获取字符内容。
     * @return 字符内容
     */
    public Character character() {
        return character;
    }
}
