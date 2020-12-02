public enum GameFormat{
    ROUND1 (1),
    ROUND2 (2),
    FINAL (3);
    int tag;
    GameFormat(int tag) {
        this.tag = tag;
    }
}