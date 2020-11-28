import model.*;

import java.util.Arrays;
import java.util.HashMap;

public class MyStrategy {

    Integer myId;
/*
    HashMap<Integer, Entity> total_units = new HashMap<>();
    HashMap<Integer, Entity> builder_units = new HashMap<>();
    HashMap<Integer, Entity> melee_units = new HashMap<>();
    HashMap<Integer, Entity> range_units = new HashMap<>();
*/

    public Action getAction(PlayerView playerView, DebugInterface debugInterface) {
        HashMap<Integer, EntityAction> actions = new HashMap<>();
        System.out.println(playerView.getCurrentTick());
        // Init
        if (playerView.getCurrentTick() == 0) {
            myId = playerView.getMyId();
        }


        for (Entity entity : playerView.getEntities()) {
            if (myId.equals(entity.getPlayerId())) {
                EntityProperties entityProperties = playerView.getEntityProperties().get(entity.getEntityType());
                MoveAction m = null;
                BuildAction b = null;
                AttackAction a = null;
                RepairAction r = null;

                if (isUnit(entity)) {
                    /*if (isUnit(entity, new EntityType[]{EntityType.BUILDER_UNIT})) {
                        m = new MoveAction(new Vec2Int(playerView.getMapSize() - 1, playerView.getMapSize() - 1), true, true);
                    }*/
                    m = new MoveAction(new Vec2Int(playerView.getMapSize() - 1, playerView.getMapSize() - 1), true, true);
                    a = new AttackAction(
                        //Arrays.stream(playerView.getEntities()).filter(e -> myId.equals(e.getEntityType()) & e.getEntityType() == EntityType.MELEE_BASE).findAny().get().getId(),
                            null,
                        new AutoAttack(
                            entityProperties.getSightRange(),
                            entity.getEntityType() == EntityType.BUILDER_UNIT ? new EntityType[]{EntityType.RESOURCE} : new EntityType[]{}
                        )
                    );
                } else if (isBase(entity)) {
                    BuildProperties buildProperties =  entityProperties.getBuild();
                    EntityType entityType = buildProperties.getOptions()[0];
                    Integer currentUnits = Math.toIntExact(Arrays.stream(playerView.getEntities()).filter(e -> myId.equals(e.getPlayerId()) & e.getEntityType() == entityType).count());
                    if ((currentUnits + 1) * playerView.getEntityProperties().get(entityType).getPopulationUse() <= entityProperties.getPopulationProvide()) {
                        b = new BuildAction(
                                entityType, new Vec2Int(
                                        entity.getPosition().getX() + entityProperties.getSize(),
                                        entity.getPosition().getY() + entityProperties.getSize() - 1
                                )
                        );
                        System.out.println(entity.getEntityType() + " " + entityType + " " + currentUnits + " " +
                                playerView.getEntityProperties().get(entityType).getPopulationUse() + " " + entityProperties.getPopulationProvide() + " " +
                                        (entity.getPosition().getX() + entityProperties.getSize()) + " " +
                                        (entity.getPosition().getY() + entityProperties.getSize() - 1)
                                );
                    }
                }
                actions.put(entity.getId(), new EntityAction(m, b, a, r));
            }
        }
        return new Action(actions);
    }

    private boolean isBase(Entity entity) {
        for (EntityType entityType : new EntityType[]{EntityType.BUILDER_BASE, EntityType.MELEE_BASE, EntityType.RANGED_BASE}) {
            if (entity.getEntityType().equals(entityType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUnit(Entity entity) {
        for (EntityType entityType : new EntityType[]{EntityType.BUILDER_UNIT, EntityType.MELEE_UNIT, EntityType.RANGED_UNIT}) {
            if (entity.getEntityType().equals(entityType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isType(Entity entity, EntityType[] entityTypes) {
        for (EntityType entityType : entityTypes) {
            if (entity.getEntityType().equals(entityType)) {
                return true;
            }
        }
        return false;
    }

    public void debugUpdate(PlayerView playerView, DebugInterface debugInterface) {
        debugInterface.send(new DebugCommand.Clear());
        debugInterface.getState();
    }
}