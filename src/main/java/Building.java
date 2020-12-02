import model.EntityType;
import model.Vec2Int;

class Building {
    private final EntityType type;
    private final Vec2Int position;

    public Building(EntityType type, Vec2Int position) {
        this.type = type;
        this.position = position;
    }

    public EntityType getType() {
        return type;
    }

    public Vec2Int getPosition() {
        return position;
    }
}
