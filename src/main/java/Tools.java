import model.Vec2Int;

import java.util.ArrayList;
import java.util.List;

class Tools {
    public static boolean equalVec(Vec2Int a, Vec2Int b) {
        return (a.getX() == b.getX() && a.getY() == b.getY());
    }

    public static boolean intersectionRect(Vec2Int a0, Vec2Int a1, Vec2Int b0, Vec2Int b1) {
        return (Math.max(a0.getX(), b0.getX()) - Math.min(a1.getX(), b1.getX()) < 0) &&
                (Math.max(a0.getY(), b0.getY()) - Math.min(a1.getY(), b1.getY()) < 0);
    }

    public static double getSqrtDistance(Vec2Int a, Vec2Int b) {
        return Math.sqrt((b.getY() - a.getY()) * (b.getY() - a.getY()) + (b.getX() - a.getX()) * (b.getX() - a.getX()));
    }

    private static int getManhattanDistance(Vec2Int a, Vec2Int b) {
        return Math.abs(b.getY() - a.getY()) + Math.abs(b.getX() - a.getX());
    }

    public static Vec2Int getMinimalPos(Vec2Int bPos, List<Vec2Int> availablePos) {
        int minDistance = 99;
        int curDistance = 0;
        Vec2Int minPos = new Vec2Int();
        for (Vec2Int aPos : availablePos) {
            curDistance = getManhattanDistance(aPos, bPos);
            if (minDistance > curDistance) {
                minDistance = curDistance;
                minPos = aPos;
            }
        }
        return minPos;
    }

    public static List<Vec2Int> getPositionAroundBase(Vec2Int position, Integer size) {
        List<Vec2Int> availablePos = new ArrayList<>();
        int mapSize = 80; // TODO Это надо перенести. Я про процедуру в целом.
        int maxX = position.getX() + size;
        int maxY = position.getY() + size;
        int minX = position.getX() - 1;
        int minY = position.getY() - 1;
        for (int i = minX + 1; i <= maxX - 1; i++) {
            if (0 <= i & i < mapSize & 0 <= minY & minY < mapSize) {
                availablePos.add(new Vec2Int(i, minY));
            }
            if (0 <= i & i < mapSize & 0 <= maxY & maxY < mapSize) {
                availablePos.add(new Vec2Int(i, maxY));
            }
        }
        for (int i = minY + 1; i <= maxY - 1; i++) {
            if (0 <= i & i < mapSize & 0 <= minX & minX < mapSize) {
                availablePos.add(new Vec2Int(minX, i));
            }
            if (0 <= i & i < mapSize & 0 <= maxX & maxX < mapSize) {
                availablePos.add(new Vec2Int(maxX, i));
            }
        }
        // TODO Отсеевать значения точек, на которых уже построены здания или есть ресурсы.
        return availablePos;
    }
}
