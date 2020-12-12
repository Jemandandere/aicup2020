import model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class GameMap {
    PlayerView playerView;

    private final boolean[][] map;

    public GameMap(PlayerView playerView) {
        this.playerView = playerView;
        this.map = new boolean[playerView.getMapSize()][playerView.getMapSize()];
    }

    public Integer getSize() {
        return playerView.getMapSize();
    }

    public void clear() {
        for (boolean[] booleans : map) {
            Arrays.fill(booleans, false);
        }
    }

    public void refresh(Entity[] entities, Map<EntityType, EntityProperties> properties) {
        clear();
        for (Entity entity : entities) {
            for (int i = entity.getPosition().getX(); i < entity.getPosition().getX() + properties.get(entity.getEntityType()).getSize(); i++) {
                for (int j = entity.getPosition().getY(); j < entity.getPosition().getY() + properties.get(entity.getEntityType()).getSize(); j++) {
                    map[i][j] = true; /*switch (entity.getEntityType()) {
                        case BUILDER_UNIT, MELEE_UNIT, RANGED_UNIT -> false;
                        case BUILDER_BASE, MELEE_BASE, RANGED_BASE, HOUSE, TURRET, WALL, RESOURCE -> true;
                    };*/
                }
            }
        }
    }

    public boolean isFree(Vec2Int pos) {
        return isFree(pos.getX(),pos.getY());
    }

    public boolean isFree(Integer x, Integer y) {
        return !map[x][y];
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

    private double getSqrtDistance(Vec2Int a, Vec2Int b) {
        return Math.sqrt((b.getY() - a.getY()) * (b.getY() - a.getY()) + (b.getX() - a.getX()) * (b.getX() - a.getX()));
    }

    private int getManhattanDistance(Vec2Int a, Vec2Int b) {
        return Math.abs(b.getY() - a.getY()) + Math.abs(b.getX() - a.getX());
    }

    public Vec2Int getMinimalPos(Vec2Int entityPosition, Vec2Int targetPosition) {
        return getMinimalPos(entityPosition, targetPosition, 1);
    }

    public Vec2Int getMinimalPos(Vec2Int startPosition, Vec2Int endPosition, Integer endSize) {
        Vec2Int minPos = startPosition;
        int minDistance = 999;
        int curDistance;
        List<Vec2Int> availablePositions = getAvailablePositions(endPosition, endSize);
        for (Vec2Int availablePosition : availablePositions) {
            curDistance = getManhattanDistance(startPosition, availablePosition);
            if (minDistance > curDistance) {
                minDistance = curDistance;
                minPos = availablePosition;
            }
        }
        return minPos;
    }

    public Entity getNearest(Map<Integer, Entity> entities, Vec2Int endPosition, Integer endSize) {
        Entity nearestEntity = null;
        int minDistance = 999;
        int curDistance;
        List<Vec2Int> availablePositions = getAvailablePositions(endPosition, endSize);
        for (Entity entity : entities.values()) {
            for (Vec2Int availablePosition : availablePositions) {
                curDistance = getManhattanDistance(entity.getPosition(), availablePosition);
                if (minDistance > curDistance) {
                    minDistance = curDistance;
                    nearestEntity = entity;
                }
            }
        }
        return nearestEntity;
    }

    private List<Vec2Int> getAvailablePositions(Vec2Int position, Integer size) {
        List<Vec2Int> availablePositions = new ArrayList<>();
        int mapSize = getSize();
        int minY = position.getY() - 1;
        int minX = position.getX() - 1;
        int maxY = position.getY() + size;
        int maxX = position.getX() + size;
        for (int i = minX + 1; i <= maxX - 1; i++) {
            if (0 <= i && i < mapSize && 0 <= minY && minY < mapSize & isFree(i, minY)) {
                availablePositions.add(new Vec2Int(i, minY));
            }
            if (0 <= i && i < mapSize && 0 <= maxY && maxY < mapSize & isFree(i, maxY)) {
                availablePositions.add(new Vec2Int(i, maxY));
            }
        }
        for (int i = minY + 1; i <= maxY - 1; i++) {
            if (0 <= i && i < mapSize && 0 <= minX && minX < mapSize & isFree(minX, i)) {
                availablePositions.add(new Vec2Int(minX, i));
            }
            if (0 <= i && i < mapSize && 0 <= maxX && maxX < mapSize & isFree(maxX, i)) {
                availablePositions.add(new Vec2Int(maxX, i));
            }
        }
        return availablePositions;
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
