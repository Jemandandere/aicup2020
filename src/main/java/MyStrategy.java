import model.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MyStrategy {

    Integer myId;

    public static class Ent {

        static HashMap<Integer, Entity> totalEntities = new HashMap<>();

        static HashMap<Integer, Entity> totalUnits = new HashMap<>();
        static HashMap<Integer, Entity> builderUnits = new HashMap<>();
        static HashMap<Integer, Entity> meleeUnits = new HashMap<>();
        static HashMap<Integer, Entity> rangeUnits = new HashMap<>();

        static HashMap<Integer, Entity> totalBases = new HashMap<>();
        static HashMap<Integer, Entity> builderBases = new HashMap<>();
        static HashMap<Integer, Entity> meleeBases = new HashMap<>();
        static HashMap<Integer, Entity> rangeBases = new HashMap<>();

        static HashMap<Integer, Entity> totalHouse = new HashMap<>();
        static HashMap<Integer, Entity> totalTurrels = new HashMap<>();
        static HashMap<Integer, Entity> totalWalls = new HashMap<>();

        static void update(Set<Entity> newEntities) {
            Set<Integer> oldEntities = new HashSet<Integer>(totalEntities.keySet());

            // Если нашли значение среди старых, то просто исключаем из списка, иначе, добавим в мапы
            for (Entity newEntity : newEntities) {
                if (oldEntities.contains(newEntity.getId())) {
                    oldEntities.remove(newEntity.getId());
                } else {
                    add(newEntity);
                }
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
                    totalHouse.put(e.getId(), e);
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
            totalHouse.remove(i);
            totalTurrels.remove(i);
            totalWalls.remove(i);
        }
    }

    public Action getAction(PlayerView playerView, DebugInterface debugInterface) {
        HashMap<Integer, EntityAction> actions = new HashMap<>();
        System.out.println(playerView.getCurrentTick());
        // Инициализируемся
        if (playerView.getCurrentTick() == 0) {
            myId = playerView.getMyId();
        }

        // Все сущности разместим в удобном хранилище
        Ent.update(Arrays.stream(playerView.getEntities()).filter(e -> myId.equals(e.getPlayerId())).collect(Collectors.toSet()));

        for (Integer i : Ent.totalUnits.keySet()) {
            Entity entity = Ent.totalUnits.get(i);
            EntityProperties entityProperties = playerView.getEntityProperties().get(entity.getEntityType());
            MoveAction m = new MoveAction(new Vec2Int(playerView.getMapSize() - 1, playerView.getMapSize() - 1), true, true);
            AttackAction a = new AttackAction(
                    null,
                    new AutoAttack(
                            entityProperties.getSightRange(),
                            entity.getEntityType() == EntityType.BUILDER_UNIT ? new EntityType[]{EntityType.RESOURCE} : new EntityType[]{}
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
            if ((currentUnits + 1) * playerView.getEntityProperties().get(entityType).getPopulationUse() <= entityProperties.getPopulationProvide()) {
                b = new BuildAction(
                    entityType,
                    new Vec2Int(
                        entity.getPosition().getX() + entityProperties.getSize(),
                        entity.getPosition().getY() + entityProperties.getSize() - 1
                    )
                );
            }
            actions.put(i, new EntityAction(null, b, null, null));
        }

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


        return new Action(actions);
    }

    public void debugUpdate(PlayerView playerView, DebugInterface debugInterface) {
        debugInterface.send(new DebugCommand.Clear());
        debugInterface.getState();
    }
}