package io.github.pourya_moghaddam.echo.vote;

public enum VoteDirection {
    UP(1), DOWN(-1), NONE(0);

    private final int value;

    VoteDirection(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
