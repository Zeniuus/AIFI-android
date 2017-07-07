package com.zeniuus.www.reactiontagging.objects;

import com.zeniuus.www.reactiontagging.types.Emoji;

/**
 * Created by zeniuus on 2017. 7. 5..
 */

public class EmojiFeedback {
    private int startTime;
    private Emoji emojiType;

    public EmojiFeedback(int startTime, Emoji emojiType) {
        this.startTime = startTime;
        this.emojiType = emojiType;
    }

    public int getStartTime() { return startTime; }

    public Emoji getEmojiType() { return emojiType; }
}
