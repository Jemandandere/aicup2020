import model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class EntityManager {
    private PlayerView playerView;
    private Map<Integer, Entity> entity;

    private MoveAction moveAction;
    private BuildAction buildAction;
    private boolean build;
    private EntityType buildEntityType;
    private Vec2Int buildPosition;
    private AttackAction attackAction;
    private RepairAction repairAction;

    public EntityManager(Map<Integer, Entity> entity, PlayerView playerView) {
        this.playerView = playerView;
        this.entity = entity;
        this.moveAction = null;
        this.buildAction = null;
        build = false;
        buildEntityType = null;
        buildPosition = null;
        this.attackAction = null;
        this.repairAction = null;
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

    public EntityManager getNearest(Vec2Int endPoint) {
        return getNearest(endPoint, 1);
    }

    public EntityManager getNearest(Vec2Int endPoint, Integer size) {
        return getNearest(endPoint, size, false);
    }

    public EntityManager getNearest(Vec2Int endPoint, boolean move) {
        return getNearest(endPoint, 1, move);
    }

    public EntityManager getNearest(Vec2Int endPoint, Integer size, boolean move) {
        Map<Integer, Entity> filtered = new HashMap<>();
        double minDistance = 99;
        double curDistance = 0;
        Vec2Int minPos = null;
        List<Vec2Int> positionAroundBase = Tools.getPositionAroundBase(endPoint, size);
        for (Map.Entry<Integer, Entity> entry : entity.entrySet()) {
            for (Vec2Int posA : positionAroundBase) {
                curDistance = Tools.getSqrtDistance(entry.getValue().getPosition(), posA);
                if (minDistance > curDistance) {
                    minDistance = curDistance;
                    minPos = posA;
                    filtered.clear();
                    filtered.put(entry.getKey(), entry.getValue());
                }
            }
        }
        if (minPos != null) {
            move(minPos);
        }
        entity = filtered;
        return this;
    }

    // Фильтруем по ID. Либо выбираем, либо оставляем
    public EntityManager filter(Set<Integer> filterIds) {
        return filter(filterIds, true);
    }

    public EntityManager filter(Set<Integer> filterIds, boolean remove) {
        if (remove) {
            for (Integer filterId : filterIds) {
                entity.remove(filterId);
            }
        } else {
            Map<Integer, Entity> filtered = new HashMap<>();
            for (Integer filterId : filterIds) {
                filtered.put(entity.get(filterId).getId(), entity.get(filterId));
            }
            entity = filtered;
        }
        return this;
    }

    // Фильтруем какое-то количество
    public EntityManager filter(int count) {
        if (count > 0) {
            Map<Integer, Entity> filtered = new HashMap<>();
            for (Map.Entry<Integer, Entity> entry : entity.entrySet()) {
                filtered.put(entry.getKey(), entry.getValue());
                if (filtered.size() == count) {
                    break;
                }
            }
            this.entity = filtered;
        }
        return this;
    }

    public Integer count() {
        return entity.size();
    }

    // По умолчанию ломимся до хаты
    public EntityManager move() {
        return move(5, 5);
    }

    // Можем идти как по координатам
    public EntityManager move(Integer x, Integer y) {
        return move(new Vec2Int(x, y));
    }

    // Так и в точку
    public EntityManager move(Vec2Int target) {
        return move(target, true, false);
    }

    // Даже с особенными параметрами
    public EntityManager move(Vec2Int target, boolean findClosestPosition, boolean breakThrough) {
        this.moveAction = new MoveAction(target, findClosestPosition, breakThrough);
        return this;
    }

    // Месить всю карту
    public EntityManager attack() {
        return attack(new EntityType[]{});
    }

    // Для рабочих
    public EntityManager attack(EntityType[] validTargets) {
        return attack(new AutoAttack(playerView.getMaxPathfindNodes(), validTargets));
    }

    // Для целевой атаки
    public EntityManager attack(Integer target) {
        return attack(target, null);
    }

    // Для особенной автоатаки
    public EntityManager attack(AutoAttack autoAttack) {
        return attack(null, autoAttack);
    }

    // А это просто объединяющая
    public EntityManager attack(Integer target, AutoAttack autoAttack) {
        this.attackAction = new AttackAction(target, autoAttack);
        return this;
    }

    // Эти для зданий
    public EntityManager build() {
        return build((Vec2Int) null);
    }

    public EntityManager build(Vec2Int position) {
        return build(null, position);
    }

    // Это для строителей
    public EntityManager build(Building building) {
        return build(building.getType(), building.getPos());
    }

    public EntityManager build(EntityType entityType, Vec2Int position) {
        this.build = true;
        this.buildEntityType = entityType;
        this.buildPosition = position;
        return this;
    }

    // Починка не может быть безцельной, так что это единственный вариант
    public EntityManager repair(Integer target) {
        repairAction = new RepairAction(target);
        return this;
    }

    public Map<Integer, EntityAction> make() {
        Map<Integer, EntityAction> actions = new HashMap<>();
        for (Map.Entry<Integer, Entity> entry : entity.entrySet()) {
            if (build) {
                if (buildEntityType == null) {
                    buildEntityType = playerView.getEntityProperties().get(entry.getValue().getEntityType()).getBuild().getOptions()[0];
                }
                if (buildPosition == null) {
                    buildPosition = new Vec2Int(
                            entry.getValue().getPosition().getX() + playerView.getEntityProperties().get(entry.getValue().getEntityType()).getSize(),
                            entry.getValue().getPosition().getY() + playerView.getEntityProperties().get(entry.getValue().getEntityType()).getSize() - 1
                    );
                }
                if (isUnit(entry.getValue()) && moveAction == null) {
                    move(
                            Tools.getMinimalPos(entry.getValue().getPosition(),
                                    Tools.getPositionAroundBase(buildPosition, playerView.getEntityProperties().get(buildEntityType).getSize())
                            ));
                }
                buildAction = new BuildAction(buildEntityType, buildPosition);
            }
            actions.put(entry.getKey(), new EntityAction(moveAction, buildAction, attackAction, repairAction));
        }
        return actions;
    }

    public Map<Integer, Entity> getEntity() {
        return entity;
    }
}
