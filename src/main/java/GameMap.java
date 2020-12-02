import model.*;

import java.util.Arrays;
import java.util.Map;

class GameMap {
    PlayerView playerView;

    private boolean[][] map;

    public GameMap(PlayerView playerView) {
        this.playerView = playerView;
        this.map = new boolean[playerView.getMapSize()][playerView.getMapSize()];
    }

    public Integer getSize() {
        return playerView.getMapSize();
    }

    public void clear() {
        for (int i = 0; i < map.length; i++) {
            Arrays.fill(map[i], false);
        }
    }

    public void refresh(Entity[] entities, Map<EntityType, EntityProperties> properties) {
        clear();
        for (Entity entity : entities) {
            for (int i = entity.getPosition().getX(); i < entity.getPosition().getX() + properties.get(entity.getEntityType()).getSize(); i++) {
                for (int j = entity.getPosition().getY(); j < entity.getPosition().getY() + properties.get(entity.getEntityType()).getSize(); j++) {
                    map[i][j] = switch (entity.getEntityType()) {
                        case BUILDER_UNIT -> false;
                        case MELEE_UNIT -> false;
                        case RANGED_UNIT -> false;
                        case BUILDER_BASE -> true;
                        case MELEE_BASE -> true;
                        case RANGED_BASE -> true;
                        case HOUSE -> true;
                        case TURRET -> true;
                        case WALL -> true;
                        case RESOURCE -> true;
                    };
                }
            }
        }
    }

    public boolean isFree(Vec2Int pos, Integer size) {
        boolean free = true;
        for (int i = pos.getX(); i < pos.getX() + size; i++) {
            for (int j = pos.getY(); j < pos.getY() + size; j++) {
                if (map[i][j]) {
                    free = false;
                    break;
                }
            }
        }
        return free;
    }

    public void print() {
        for (int i = map.length - 1; i >= 0; i--) {
            for (int j = 0; j < map[i].length; j++) {
                System.out.print(map[i][j]); // Карта повёрнута по часовой стрелке
            }
            System.out.println();
        }
    }
}
