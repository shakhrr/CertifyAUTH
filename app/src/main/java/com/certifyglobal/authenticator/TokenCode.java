
package com.certifyglobal.authenticator;

public class TokenCode {
    private final String mCode;
    private final long mStart;
    private final long mUntil;
    private TokenCode mNext;

    public TokenCode(String code, long start, long until) {
        mCode = code;
        mStart = start;
        mUntil = until;
    }

    public TokenCode(TokenCode prev, String code, long start, long until) {
        this(code, start, until);
        prev.mNext = this;
    }

    public TokenCode(String code, long start, long until, TokenCode next) {
        this(code, start, until);
        mNext = next;
    }

    public String getCurrentCode() {
        TokenCode active = getActive(System.currentTimeMillis());
        if (active == null)
            return null;
        return active.mCode;
    }

    public int getTotalProgress() {
        long cur = System.currentTimeMillis();
        long total = getLast().mUntil - mStart;
        long state = total - (cur - mStart);
        return (int) (state * 1000 / total);
    }

    public int getCurrentProgress() {
        long cur = System.currentTimeMillis();
        TokenCode active = getActive(cur);
        if (active == null)
            return 0;

        long total = active.mUntil - active.mStart;
        long state = total - (cur - active.mStart);
        return (int) (state * 1000 / total);
    }

    private TokenCode getActive(long curTime) {
        if (curTime >= mStart && curTime < mUntil)
            return this;

        if (mNext == null)
            return null;

        return this.mNext.getActive(curTime);
    }

    private TokenCode getLast() {
        if (mNext == null)
            return this;
        return this.mNext.getLast();
    }
}
