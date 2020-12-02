import model.EntityType;

class Limits {
    private final Integer builderLimit;
    private final Integer meleeLimit;
    private final Integer rangeLimit;

    public Limits(Integer builderLimit, Integer meleeLimit, Integer rangeLimit) {
        this.builderLimit = builderLimit;
        this.meleeLimit = meleeLimit;
        this.rangeLimit = rangeLimit;
    }

    public Integer getLimit(EntityType entityType) {
        return switch (entityType) {
            case BUILDER_UNIT -> builderLimit;
            case MELEE_UNIT -> meleeLimit;
            case RANGED_UNIT -> rangeLimit;
            default -> 0;
        };
    }
}
