import model.EntityType;
import model.Vec2Int;

class Building {
    private Vec2Int pos;
    private EntityType type;

    public Building(Vec2Int pos, EntityType type) {
        this.pos = pos;
        this.type = type;
    }

    public Vec2Int getPos() {
        return pos;
    }

    public EntityType getType() {
        return type;
    }
}
