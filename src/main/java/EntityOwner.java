enum EntityOwner {
    ALL(-1),
    MY(0),
    NEW(1);
    public int tag;

    EntityOwner(int tag) {
        this.tag = tag;
    }
}
