import model.*;

import java.util.*;
import java.util.stream.Collectors;

public class MyStrategy {

    Integer myId;


    private static Map<EntityType, Integer> unitMax = new HashMap<EntityType, Integer>();

    private static List<Vec2Int> housesPositions = new ArrayList<>();
    private static Vec2Int houseToBuild;
    private static int housesCountPrev = -1;
    private static Set<Integer> houseRepaired = new HashSet<>();
    private static int repairTick = 0;
    static {
        unitMax.put(EntityType.BUILDER_UNIT, 10);
        unitMax.put(EntityType.MELEE_UNIT, 0);
        unitMax.put(EntityType.RANGED_UNIT, 99);

        housesPositions.add(new Vec2Int(8, 2));
        housesPositions.add(new Vec2Int(5, 2));
        housesPositions.add(new Vec2Int(2, 2));
        housesPositions.add(new Vec2Int(2, 5));
        housesPositions.add(new Vec2Int(2, 8));
        housesPositions.add(new Vec2Int(0, 0));
    }

    public static class Ent {

        static HashMap<Integer, Entity> totalEntities = new HashMap<>();

        static HashMap<Integer, Entity> totalUnits = new HashMap<>();
        static HashMap<Integer, Entity> builderUnits = new HashMap<>();
        static Integer builderUnitHouse = null;
        static HashMap<Integer, Entity> meleeUnits = new HashMap<>();
        static HashMap<Integer, Entity> rangeUnits = new HashMap<>();

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
                }
                add(newEntity);
            }

            // Всё что не нашлось среди новых значений считаем что было убито и исключаем из мап.
            for (Integer oldEntity : oldEntities) {
                remove(oldEntity);
            }
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
                    totalBases.put(e.getId(), e);
                    builderBases.put(e.getId(), e);
                    break;
                case MELEE_BASE:
                    totalBases.put(e.getId(), e);
                    meleeBases.put(e.getId(), e);
                    break;
                case RANGED_BASE:
                    totalBases.put(e.getId(), e);
                    rangeBases.put(e.getId(), e);
                    break;
                case HOUSE:
                    totalHouses.put(e.getId(), e);
                    break;
                case TURRET:
                    totalTurrels.put(e.getId(), e);
                    break;
                case WALL:
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
        }
        System.out.println(playerView.getCurrentTick());
        // Все сущности разместим в удобном хранилище
        Ent.update(Arrays.stream(playerView.getEntities()).filter(e -> myId.equals(e.getPlayerId())).collect(Collectors.toSet()));

        int builders_count = 0;

        // Рабочие вседа добывают, но один строит
        for (Integer i : Ent.builderUnits.keySet()) {
            Entity entity = Ent.builderUnits.get(i);
            builders_count += 1;
            // Если это наш особенный
            if ((housesPositions.size() > 0) & (builders_count == unitMax.get(EntityType.BUILDER_UNIT)) & (Ent.builderUnitHouse == null)) {
                Ent.builderUnitHouse = entity.getId();
            }
            MoveAction m = null;
            AttackAction a = null;
            BuildAction b = null;
            RepairAction r = null;
            if ((Ent.builderUnitHouse != null) && (Ent.builderUnitHouse == entity.getId())) {
                Set<Integer> houseToRepair = new HashSet<>();
                for (Entity house : Ent.totalHouses.values()) {
                    if (!house.isActive()) {
                        houseToRepair.add(house.getId());
                    }
                }
                if (houseToRepair.iterator().hasNext()){
                    r = new RepairAction(houseToRepair.iterator().next());
                } else if (housesPositions.iterator().hasNext()) {
                    if (housesCountPrev < Ent.totalHouses.values().size()) {
                        housesCountPrev = Ent.totalHouses.values().size();
                        houseToBuild = housesPositions.remove(0);
                    }
                    m = new MoveAction(new Vec2Int(houseToBuild.getX()-1, houseToBuild.getY()), true, false);
                    b = new BuildAction(EntityType.HOUSE, houseToBuild);
                } else {
                    Ent.builderUnitHouse = null;
                    unitMax.put(EntityType.BUILDER_UNIT, 15);
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
                m = new MoveAction(new Vec2Int(playerView.getMapSize()/4 - 1, playerView.getMapSize()/4 - 1), true, false);
                a = new AttackAction(
                        null,
                        new AutoAttack(
                                entityProperties.getSightRange(),
                                new EntityType[]{}
                        )
                );
            } /*else if ((10 <= Ent.rangeUnits.size()) & (Ent.rangeUnits.size() < 20)) {
                m = new MoveAction(new Vec2Int(playerView.getMapSize()/2 - 1, playerView.getMapSize()/2 - 1), true, false);
                a = new AttackAction(
                        null,
                        new AutoAttack(
                                playerView.getMapSize(), // Уничтожаем всё вокруг
                                //entityProperties.getSightRange(), // Бъем только окружающих
                                new EntityType[]{}
                        )
                );
            } */else {
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

        for (Integer i : Ent.totalBases.keySet()) {
            Entity entity = Ent.totalBases.get(i);
            EntityProperties entityProperties = playerView.getEntityProperties().get(entity.getEntityType());
            BuildProperties buildProperties =  entityProperties.getBuild();
            EntityType entityType = buildProperties.getOptions()[0];
            Integer currentUnits = Math.toIntExact(Arrays.stream(playerView.getEntities()).filter(e -> myId.equals(e.getPlayerId()) & e.getEntityType() == entityType).count());
            BuildAction b = null;
            if ((currentUnits + 1) * playerView.getEntityProperties().get(entityType).getPopulationUse() <= unitMax.get(entityType)) {
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

        for (Integer i : Ent.totalHouses.keySet()) {
            Entity entity = Ent.totalHouses.get(i);
            EntityProperties entityProperties = playerView.getEntityProperties().get(entity.getEntityType());
            System.out.println(entity.getHealth() + "/" + entityProperties.getMaxHealth() + " " + entity.isActive());
            actions.put(i, new EntityAction(null, null, null, null));
        }


        return new Action(actions);
    }

    public void debugUpdate(PlayerView playerView, DebugInterface debugInterface) {
        debugInterface.send(new DebugCommand.Clear());
        debugInterface.getState();
    }
}