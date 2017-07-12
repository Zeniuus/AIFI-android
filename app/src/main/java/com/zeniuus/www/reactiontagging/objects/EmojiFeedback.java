package com.zeniuus.www.reactiontagging.objects;

import com.zeniuus.www.reactiontagging.types.Emoji;

/**
 * Created by zeniuus on 2017. 7. 5..
 */

public class EmojiFeedback {
    private String userId;
    private int startTime;
    private Emoji emojiType;

    public EmojiFeedback(String userId, int startTime, Emoji emojiType) {
        this.userId = userId;
        this.startTime = startTime;
        this.emojiType = emojiType;
    }

    public String getUserId() { return userId; }

    public int getStartTime() { return startTime; }

    public Emoji getEmojiType() { return emojiType; }
}
