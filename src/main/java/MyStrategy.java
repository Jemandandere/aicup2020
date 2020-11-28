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
        buildQueue.add(new Buildings(new Vec2Int(2, 8), EntityType.HOUSE));
        buildQueue.add(new Buildings(new Vec2Int(2, 5), EntityType.HOUSE));
        buildQueue.add(new Buildings(new Vec2Int(2, 2), EntityType.HOUSE));
        buildQueue.add(new Buildings(new Vec2Int(5, 2), EntityType.HOUSE));
        buildQueue.add(new Buildings(new Vec2Int(8, 2), EntityType.HOUSE));
        buildQueue.add(new Buildings(new Vec2Int(10, 5), EntityType.RANGED_BASE));
        buildQueue.add(new Buildings(new Vec2Int(11, 2), EntityType.HOUSE));
        buildQueue.add(new Buildings(new Vec2Int(14, 2), EntityType.HOUSE));
        buildQueue.add(new Buildings(new Vec2Int(17, 2), EntityType.HOUSE));
    }

    public static class Ent {

        static HashMap<Integer, Entity> totalEntities = new HashMap<>();

        static HashMap<Integer, Entity> totalUnits = new HashMap<>();
        static HashMap<Integer, Entity> builderUnits = new HashMap<>();
        static Integer builderUnitHouse = null;
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
            if (builderUnitHouse == i) {
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
            limits.put(15, new Limits(10,0,5));
            limits.put(20, new Limits(10,0,10));
            limits.put(25, new Limits(10,0,15));
            limits.put(30, new Limits(10,0,20));
            limits.put(35, new Limits(15,0,20));
            limits.put(40, new Limits(15,0,25));
            limits.put(45, new Limits(15,0,30));
            limits.put(50, new Limits(15,0,35));
            limits.put(55, new Limits(15,0,40));
            limits.put(60, new Limits(20,0,40));
            limits.put(65, new Limits(20,0,45));
            limits.put(70, new Limits(20,0,50));
            limits.put(75, new Limits(20,0,55));
            limits.put(80, new Limits(20,0,60));
            limits.put(85, new Limits(20,0,65));
            limits.put(90, new Limits(20,0,70));
            limits.put(95, new Limits(20,0,75));
            limits.put(100, new Limits(20,0,80));
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
                Ent.builderUnitHouse = entity.getId();
            }
            MoveAction m = null;
            AttackAction a = null;
            BuildAction b = null;
            RepairAction r = null;
            if ((Ent.builderUnitHouse != null) && (Ent.builderUnitHouse == entity.getId())) {
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
                    m = new MoveAction(new Vec2Int(toBuild.getPos().getX() + 1, toBuild.getPos().getY() - 1), true, false);
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
            if (Ent.rangeUnits.size() < 10) {
                m = new MoveAction(new Vec2Int(14, 14), true, false);
                a = new AttackAction(
                        null,
                        new AutoAttack(
                                entityProperties.getSightRange()*2,
                                new EntityType[]{}
                        )
                );
            } else {
                m = new MoveAction(new Vec2Int(playerView.getMapSize()/2 - 1, playerView.getMapSize()/2 - 1), true, false);
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

        // Милик просто идёт на разведку и убивается, потому что может, потому что он герой, а ещё потому что он нахуй не нужен
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

        // Базы просто клепают юнитов как могут, а могут они быстро
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
                        entity.getPosition().getX(),
                        entity.getPosition().getY() + entityProperties.getSize()
                    )
                );
            }
            actions.put(i, new EntityAction(null, b, null, null));
        }

        // Туррели просто постоянно хуярят
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


        return new Action(actions);
    }

    public void debugUpdate(PlayerView playerView, DebugInterface debugInterface) {
        debugInterface.send(new DebugCommand.Clear());
        debugInterface.getState();
    }
}