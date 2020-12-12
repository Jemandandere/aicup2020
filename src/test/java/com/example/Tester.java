package com.example;

import model.Vec2Int;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Tester {

    private static class Rect {
        Vec2Int pos;
        Integer size;

        public Rect(Vec2Int pos, Integer size) {
            this.pos = pos;
            this.size = size;
        }

        public Vec2Int getPos() {
            return pos;
        }

        public Integer getSize() {
            return size;
        }

        @Override
        public String toString() {
            return "(" + pos.getX() + "," + pos.getY() + ") (" + (pos.getX() + size) + "," + (pos.getY() + size) + ")";
        }
    }

    @Test
    public void main() {
        Rect a = new Rect(new Vec2Int(1, 1), 3);
        Rect b = new Rect(new Vec2Int(2, 3), 1);
        Rect c = new Rect(new Vec2Int(4, 4), 1);
        Rect d = new Rect(new Vec2Int(3, 2), 2);
        Rect e = new Rect(new Vec2Int(9, 9), 1);
        Rect f = new Rect(new Vec2Int(4, 9), 1);
        Assertions.assertEquals(true, incec(a, b));
        Assertions.assertEquals(false, incec(a, c));
        Assertions.assertEquals(true, incec(a, d));
        Assertions.assertEquals(false, incec(b, c));
        Assertions.assertEquals(false, incec(b, d));
        Assertions.assertEquals(false, incec(c, d));
        Assertions.assertEquals(false, incec(c, e));
        Assertions.assertEquals(false, incec(c, f));
        Assertions.assertEquals(false, incec(e, f));
    }

    static boolean incec(Rect a, Rect b) {
        Integer maxLeft = Math.max(a.getPos().getX(), b.getPos().getX());
        Integer minRight = Math.min(a.getPos().getX() + a.getSize(), b.getPos().getX() + b.getSize());
        Integer maxBottom = Math.max(a.getPos().getY(), b.getPos().getY());
        Integer minTop = Math.min(a.getPos().getY() + a.getSize(), b.getPos().getY() + b.getSize());
        return (Math.max(a.getPos().getX(), b.getPos().getX()) - Math.min(a.getPos().getX() + a.getSize(), b.getPos().getX() + b.getSize()) < 0) &&
                (Math.max(a.getPos().getY(), b.getPos().getY()) - Math.min(a.getPos().getY() + a.getSize(), b.getPos().getY() + b.getSize()) < 0);
    }

}
