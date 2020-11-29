import model.*;

import java.util.*;
import java.util.stream.Collectors;

public class MyStrategy {

    Integer myId;


    private static Map<Integer, Limits> limits = new HashMap<Integer, Limits>();

    private class Limits {
        private Integer builderLimit;
        private Integer meleeLimit;
        private Integer rangeLimit;

        public Limits(Integer builderLimit, Integer meleeLimit, Integer rangeLimit) {
            this.builderLimit = builderLimit;
            this.meleeLimit = meleeLimit;
            this.rangeLimit = rangeLimit;
        }

        public Integer getLimit(EntityType entityType) {
            switch (entityType) {
                case BUILDER_UNIT:
                    return builderLimit;
                case MELEE_UNIT:
                    return meleeLimit;
                case RANGED_UNIT:
                    return rangeLimit;
                default:
                    return 0;
            }
        }
    }

    private static List<Buildings> buildQueue = new ArrayList<>();
    private static Buildings toBuild;
    private static Integer countBuildsPrev = 0;

    private static class Buildings {
        private Vec2Int pos;
        private EntityType type;

        public Buildings(Vec2Int pos, EntityType type) {
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

    static {
        buildQueue.add(new Buildings(new Vec2Int(1, 9), EntityType.HOUSE));
        buildQueue.add(new Buildings(new Vec2Int(1, 5), EntityType.HOUSE));
        buildQueue.add(new Buildings(new Vec2Int(1, 1), EntityType.HOUSE));
        buildQueue.add(new Buildings(new Vec2Int(5, 1), EntityType.HOUSE));
        buildQueue.add(new Buildings(new Vec2Int(9, 1), EntityType.HOUSE));

        buildQueue.add(new Buildings(new Vec2Int(11, 6), EntityType.HOUSE));

        buildQueue.add(new Buildings(new Vec2Int(13, 1), EntityType.HOUSE));
        buildQueue.add(new Buildings(new Vec2Int(17, 1), EntityType.HOUSE));
        buildQueue.add(new Buildings(new Vec2Int(21, 1), EntityType.HOUSE));
        buildQueue.add(new Buildings(new Vec2Int(21, 5), EntityType.HOUSE));
        buildQueue.add(new Buildings(new Vec2Int(21, 9), EntityType.HOUSE));

        buildQueue.add(new Buildings(new Vec2Int(17, 11), EntityType.HOUSE));
        buildQueue.add(new Buildings(new Vec2Int(13, 11), EntityType.HOUSE));
        buildQueue.add(new Buildings(new Vec2Int(9, 11), EntityType.HOUSE));
        buildQueue.add(new Buildings(new Vec2Int(5, 11), EntityType.HOUSE));

        buildQueue.add(new Buildings(new Vec2Int(0, 15), EntityType.TURRET));
        buildQueue.add(new Buildings(new Vec2Int(3, 15), EntityType.TURRET));
        buildQueue.add(new Buildings(new Vec2Int(6, 15), EntityType.TURRET));
        buildQueue.add(new Buildings(new Vec2Int(9, 15), EntityType.TURRET));
        buildQueue.add(new Buildings(new Vec2Int(12, 15), EntityType.TURRET));
        buildQueue.add(new Buildings(new Vec2Int(18, 15), EntityType.TURRET));
        buildQueue.add(new Buildings(new Vec2Int(21, 14), EntityType.TURRET));
        buildQueue.add(new Buildings(new Vec2Int(24, 13), EntityType.TURRET));
        buildQueue.add(new Buildings(new Vec2Int(25, 10), EntityType.TURRET));
        buildQueue.add(new Buildings(new Vec2Int(25, 7), EntityType.TURRET));
        buildQueue.add(new Buildings(new Vec2Int(25, 4), EntityType.TURRET));
        buildQueue.add(new Buildings(new Vec2Int(25, 1), EntityType.TURRET));

    }

    public static class Ent {

        static HashMap<Integer, Entity> totalEntities = new HashMap<>();

        static HashMap<Integer, Entity> totalUnits = new HashMap<>();
        static HashMap<Integer, Entity> builderUnits = new HashMap<>();
        static Entity builderUnitHouse = null;
        static HashMap<Integer, Entity> meleeUnits = new HashMap<>();
        static HashMap<Integer, Entity> rangeUnits = new HashMap<>();

        static HashMap<Integer, Entity> totalBuildings = new HashMap<>();
        private static int countBuilds = 0; // Сколько зданий было построено, изначальные тоже считаются
        static HashMap<Integer, Entity> totalBases = new HashMap<>();
        static HashMap<Integer, Entity> builderBases = new HashMap<>();
        static HashMap<Integer, Entity> meleeBases = new HashMap<>();
        static HashMap<Integer, Entity> rangeBases = new HashMap<>();

        static HashMap<Integer, Entity> totalHouses = new HashMap<>();
        static HashMap<Integer, Entity> totalTurrels = new HashMap<>();
        static HashMap<Integer, Entity> totalWalls = new HashMap<>();

        static void update(Set<Entity> newEntities) {
            Set<Integer> oldEntities = new HashSet<Integer>(totalEntities.keySet());

            // Если нашли значение среди старых, то просто исключаем из списка, иначе, добавим в мапы
            for (Entity newEntity : newEntities) {
                if (oldEntities.contains(newEntity.getId())) {
                    oldEntities.remove(newEntity.getId());
                } else {
                    if (isBuildings(newEntity)) {
                        countBuilds += 1;
                    }
                }
                add(newEntity);
            }

            // Всё что не нашлось среди новых значений считаем что было убито и исключаем из мап.
            for (Integer oldEntity : oldEntities) {
                remove(oldEntity);
            }
        }

        private static boolean isBuildings(Entity entity) {
            for (EntityType entityType : new EntityType[]{
                    EntityType.BUILDER_BASE,
                    EntityType.MELEE_BASE,
                    EntityType.RANGED_BASE,
                    EntityType.HOUSE,
                    EntityType.TURRET,
                    EntityType.WALL,
            }) {
                if (entity.getEntityType().equals(entityType)) {
                    return true;
                }
            }
            return false;
        }

        private static void add(Entity e) {
            totalEntities.put(e.getId(), e);
            switch (e.getEntityType()) {
                case BUILDER_UNIT:
                    totalUnits.put(e.getId(), e);
                    builderUnits.put(e.getId(), e);
                    break;
                case MELEE_UNIT:
                    totalUnits.put(e.getId(), e);
                    meleeUnits.put(e.getId(), e);
                    break;
                case RANGED_UNIT:
                    totalUnits.put(e.getId(), e);
                    rangeUnits.put(e.getId(), e);
                    break;
                case BUILDER_BASE:
                    totalBuildings.put(e.getId(), e);
                    totalBases.put(e.getId(), e);
                    builderBases.put(e.getId(), e);
                    break;
                case MELEE_BASE:
                    totalBuildings.put(e.getId(), e);
                    totalBases.put(e.getId(), e);
                    meleeBases.put(e.getId(), e);
                    break;
                case RANGED_BASE:
                    totalBuildings.put(e.getId(), e);
                    totalBases.put(e.getId(), e);
                    rangeBases.put(e.getId(), e);
                    break;
                case HOUSE:
                    totalBuildings.put(e.getId(), e);
                    totalHouses.put(e.getId(), e);
                    break;
                case TURRET:
                    totalBuildings.put(e.getId(), e);
                    totalTurrels.put(e.getId(), e);
                    break;
                case WALL:
                    totalBuildings.put(e.getId(), e);
                    totalWalls.put(e.getId(), e);
                    break;
            }
        }

        private static void remove(Integer i) {
            totalEntities.remove(i);
            totalUnits.remove(i);
            builderUnits.remove(i);
            meleeUnits.remove(i);
            rangeUnits.remove(i);
            totalBases.remove(i);
            builderBases.remove(i);
            meleeBases.remove(i);
            rangeBases.remove(i);
            totalHouses.remove(i);
            totalTurrels.remove(i);
            totalWalls.remove(i);
            if (builderUnitHouse != null && builderUnitHouse.getId() == i) {
                builderUnitHouse = null;
            }
        }
    }

    public Action getAction(PlayerView playerView, DebugInterface debugInterface) {
        HashMap<Integer, EntityAction> actions = new HashMap<>();
        // Инициализируемся
        if (playerView.getCurrentTick() == 0) {
            myId = playerView.getMyId();
            // Зададим лимиты
            limits.put(0, new Limits(0,0,0));
            limits.put(5, new Limits(5,0,0));
            limits.put(10, new Limits(10,0,0));
            limits.put(15, new Limits(14,0,1));
            limits.put(20, new Limits(19,0,1));
            limits.put(25, new Limits(24,0,1));
            limits.put(30, new Limits(25,0,5));
            limits.put(35, new Limits(25,0,10));
            limits.put(40, new Limits(25,0,15));
            limits.put(45, new Limits(25,0,20));
            limits.put(50, new Limits(25,0,25));
            limits.put(55, new Limits(30,0,25));
            limits.put(60, new Limits(35,0,25));
            limits.put(65, new Limits(35,0,30));
            limits.put(70, new Limits(35,0,35));
            limits.put(75, new Limits(35,0,40));
            limits.put(80, new Limits(35,0,45));
            limits.put(85, new Limits(35,0,50));
            limits.put(90, new Limits(35,0,55));
            limits.put(95, new Limits(35,0,60));
            limits.put(100, new Limits(35,0,65));
        }

        // Все сущности разместим в удобном хранилище
        Ent.update(Arrays.stream(playerView.getEntities()).filter(e -> myId.equals(e.getPlayerId())).collect(Collectors.toSet()));

        int currentLimit = 0; // Максимальное количество лимита
        for (Entity entity : Ent.totalBuildings.values()) {
            currentLimit += playerView.getEntityProperties().get(entity.getEntityType()).getPopulationProvide();
        }

        int builders_count = 0;
        // Рабочие вседа добывают, но один строит
        for (Integer i : Ent.builderUnits.keySet()) {
            Entity entity = Ent.builderUnits.get(i);
            builders_count += 1;
            // Если это наш особенный

            if ((buildQueue.size() > 0) & (builders_count == limits.get(currentLimit).getLimit(EntityType.BUILDER_UNIT)) & (Ent.builderUnitHouse == null)) {
                Ent.builderUnitHouse = entity;
            }
            MoveAction m = null;
            AttackAction a = null;
            BuildAction b = null;
            RepairAction r = null;
            if ((Ent.builderUnitHouse != null) && (Ent.builderUnitHouse.getId() == entity.getId())) {
                // Перечитаем строителя, иначе он считает всё от начально точки и в поисках оптимальной точки строительства нет смысла
                Ent.builderUnitHouse = entity;

                Set<Integer> toActivate = new HashSet<>();
                for (Entity e : Ent.totalBuildings.values()) {
                    if (!e.isActive()) {
                        toActivate.add(e.getId());
                    }
                }
                if (toActivate.iterator().hasNext()){
                    r = new RepairAction(toActivate.iterator().next());
                    toBuild = null;
                } else if ((buildQueue.iterator().hasNext()) | (toBuild != null)) {
                    if (countBuildsPrev < Ent.countBuilds) {
                        countBuildsPrev = Ent.countBuilds;
                        toBuild = buildQueue.remove(0);
                    }
                    m = new MoveAction(getMinimalPos(Ent.builderUnitHouse.getPosition(), getAvailablePos(toBuild, playerView)), true, false);
                    b = new BuildAction(toBuild.getType(), toBuild.getPos());
                } else {
                    a = new AttackAction(
                            null,
                            new AutoAttack(
                                    playerView.getMapSize(),
                                    new EntityType[]{EntityType.RESOURCE}
                            )
                    );
                }
            } else {
                a = new AttackAction(
                        null,
                        new AutoAttack(
                                playerView.getMapSize(),
                                new EntityType[]{EntityType.RESOURCE}
                        )
                );
            }
            actions.put(i, new EntityAction(m, b, a, r));
        }

        // Рэнжи кооперируются и разъебывают
        for (Integer i : Ent.rangeUnits.keySet()) {
            Entity entity = Ent.rangeUnits.get(i);
            EntityProperties entityProperties = playerView.getEntityProperties().get(entity.getEntityType());
            MoveAction m = null;
            AttackAction a = null;
            if (Ent.rangeUnits.size() < 30) {
                m = new MoveAction(new Vec2Int(17, 17), true, false);
                a = new AttackAction(
                        null,
                        new AutoAttack(
                                entityProperties.getSightRange() * 2,
                                new EntityType[]{}
                        )
                );
            } else if (Ent.meleeBases.values().size() > 0) {
                Entity t = Ent.meleeBases.values().stream().findFirst().get();
                m = new MoveAction(t.getPosition(), true, false);
                a = new AttackAction(
                        t.getId(),
                        null
                );
            } else {
                m = new MoveAction(new Vec2Int(playerView.getMapSize() / 2 - 1, playerView.getMapSize() / 2 - 1), true, false);
                a = new AttackAction(
                        null,
                        new AutoAttack(
                                playerView.getMapSize() * 2, // Уничтожаем всё вокруг
                                //entityProperties.getSightRange(), // Бъем только окружающих
                                new EntityType[]{}
                        )
                );
            }
            actions.put(i, new EntityAction(m, null, a, null));
        }

        // Милик идёт на разведку и убивается, потому что может, потому что он герой, а ещё потому что он нахуй не нужен
        for (Integer i : Ent.meleeUnits.keySet()) {
            Entity entity = Ent.meleeUnits.get(i);
            EntityProperties entityProperties = playerView.getEntityProperties().get(entity.getEntityType());
            MoveAction m = new MoveAction(
                new Vec2Int(
                    playerView.getMapSize() - (Ent.builderBases.entrySet().iterator().hasNext() ? Ent.builderBases.entrySet().iterator().next().getValue().getPosition().getX() : 5),
                    playerView.getMapSize() - (Ent.builderBases.entrySet().iterator().hasNext() ? Ent.builderBases.entrySet().iterator().next().getValue().getPosition().getY() : 5)
                ),true, true);
            AttackAction a = new AttackAction(
                    null,
                    new AutoAttack(
                            playerView.getMapSize(),
                            new EntityType[]{}
                    )
            );
            actions.put(i, new EntityAction(m, null, a, null));
        }

        // Базы клепают юнитов как могут, а могут они быстро
        for (Integer i : Ent.totalBases.keySet()) {
            Entity entity = Ent.totalBases.get(i);
            EntityProperties entityProperties = playerView.getEntityProperties().get(entity.getEntityType());
            BuildProperties buildProperties =  entityProperties.getBuild();
            EntityType entityType = buildProperties.getOptions()[0];
            Integer currentUnits = Math.toIntExact(Arrays.stream(playerView.getEntities()).filter(e -> myId.equals(e.getPlayerId()) & e.getEntityType() == entityType).count());
            BuildAction b = null;
            if ((currentUnits + 1) * playerView.getEntityProperties().get(entityType).getPopulationUse() <= limits.get(currentLimit).getLimit(entityType)) {
                b = new BuildAction(
                    entityType,
                    new Vec2Int(
                        entity.getPosition().getX() + (entity.getEntityType() == EntityType.BUILDER_BASE ? -1 : entityProperties.getSize()),
                            entity.getPosition().getY()
                    )
                );
            }
            actions.put(i, new EntityAction(null, b, null, null));
        }

        // Туррели постоянно хуярят
        for (Integer i : Ent.totalTurrels.keySet()) {
            Entity entity = Ent.totalTurrels.get(i);
            EntityProperties entityProperties = playerView.getEntityProperties().get(entity.getEntityType());
            AttackAction a = new AttackAction(
                    null,
                    new AutoAttack(
                            entityProperties.getSightRange(),
                            new EntityType[]{}
                    )
            );
            actions.put(i, new EntityAction(null, null, a, null));
        }

        // Здания просто стоят и выглядят ахуенно
        for (Integer i : Ent.totalHouses.keySet()) {
            Entity entity = Ent.totalHouses.get(i);
            EntityProperties entityProperties = playerView.getEntityProperties().get(entity.getEntityType());
            actions.put(i, new EntityAction(null, null, null, null));
        }
        //System.out.println("---------------------------------");
        return new Action(actions);
    }

    private double getSqrtDistance(Vec2Int a, Vec2Int b) {
        return Math.sqrt((b.getY() - a.getY()) * (b.getY() - a.getY()) + (b.getX() - a.getX()) * (b.getX() - a.getX()));
    }

    private Vec2Int getMinimalPos(Vec2Int bPos, List<Vec2Int> availablePos) {
        double minDistance = 9999;
        double curDistance = 0;
        Vec2Int minPos = new Vec2Int();
        for (Vec2Int aPos : availablePos) {
            curDistance = getSqrtDistance(aPos, bPos);
            //System.out.println("(" +aPos.getX() + ", " + aPos.getY() + ") cur: " + curDistance + " min: " + minDistance);
            if (minDistance > curDistance) {
                minDistance = curDistance;
                minPos = aPos;
            }
        }
        //System.out.println(minPos.getX() + " " + minPos.getY());
        return minPos;
    }

    private List<Vec2Int> getAvailablePos(Buildings toBuild, PlayerView playerView) {
        List<Vec2Int> availablePos = new ArrayList<>();
        int buildSize = playerView.getEntityProperties().get(toBuild.type).getSize();
        int mapSize = playerView.getMapSize();
        int maxX = toBuild.getPos().getX() + buildSize;
        int maxY = toBuild.getPos().getY() + buildSize;
        int minX = toBuild.getPos().getX() - 1 ;
        int minY = toBuild.getPos().getY() -1 ;
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
        // TODO Отсеевать значения точек, на которых уже построены здания
        /*for (Vec2Int vec2Int : availablePos) {
            System.out.println(vec2Int.getX() + " " + vec2Int.getY());
        }*/
        return availablePos;
    }

    public void debugUpdate(PlayerView playerView, DebugInterface debugInterface) {
        debugInterface.send(new DebugCommand.Clear());
        debugInterface.getState();
    }
}