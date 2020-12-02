public enum GamePhase {
    START(0),
    ALLOW_BUILD(1);

    public int tag;
    GamePhase(int tag) {
        this.tag = tag;
    }
}

