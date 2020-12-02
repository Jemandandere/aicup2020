import model.Entity;
import model.EntityProperties;
import model.EntityType;
import model.PlayerView;

import java.util.*;

class EntityStorage {
    PlayerView playerView;

    private Map<Integer, Entity> entities;
    private Set<Integer> oldEntities;

    private Map<Integer, Entity> allBuildUnit;
    private Map<Integer, Entity> allMeleeUnit;
    private Map<Integer, Entity> allRangeUnit;
    private Map<Integer, Entity> allBuildBase;
    private Map<Integer, Entity> allMeleeBase;
    private Map<Integer, Entity> allRangeBase;
    private Map<Integer, Entity> allHouse;
    private Map<Integer, Entity> allTurret;
    private Map<Integer, Entity> allWall;
    private Map<Integer, Entity> allResource;

    private Map<Integer, Entity> myBuildUnit;
    private Map<Integer, Entity> myMeleeUnit;
    private Map<Integer, Entity> myRangeUnit;
    private Map<Integer, Entity> myTotalBase;
    private Map<Integer, Entity> myBuildBase;
    private Map<Integer, Entity> myMeleeBase;
    private Map<Integer, Entity> myRangeBase;
    private Map<Integer, Entity> myHouse;
    private Map<Integer, Entity> myTurret;
    private Map<Integer, Entity> myWall;

    private Map<Integer, Entity> newBuildUnit;
    private Map<Integer, Entity> newMeleeUnit;
    private Map<Integer, Entity> newRangeUnit;
    private Map<Integer, Entity> newBuildBase;
    private Map<Integer, Entity> newMeleeBase;
    private Map<Integer, Entity> newRangeBase;
    private Map<Integer, Entity> newHouse;
    private Map<Integer, Entity> newTurret;
    private Map<Integer, Entity> newWall;


    public EntityStorage(PlayerView playerView) {
        this.playerView = playerView;

        entities = new HashMap<>();
        oldEntities = new HashSet<>();

        allBuildUnit = new HashMap<>();
        allMeleeUnit = new HashMap<>();
        allRangeUnit = new HashMap<>();
        allBuildBase = new HashMap<>();
        allMeleeBase = new HashMap<>();
        allRangeBase = new HashMap<>();
        allHouse = new HashMap<>();
        allTurret = new HashMap<>();
        allWall = new HashMap<>();
        allResource = new HashMap<>();

        myBuildUnit = new HashMap<>();
        myMeleeUnit = new HashMap<>();
        myRangeUnit = new HashMap<>();
        myTotalBase = new HashMap<>();
        myBuildBase = new HashMap<>();
        myMeleeBase = new HashMap<>();
        myRangeBase = new HashMap<>();
        myHouse = new HashMap<>();
        myTurret = new HashMap<>();
        myWall = new HashMap<>();

        newBuildUnit = new HashMap<>();
        newMeleeUnit = new HashMap<>();
        newRangeUnit = new HashMap<>();
        newBuildBase = new HashMap<>();
        newMeleeBase = new HashMap<>();
        newRangeBase = new HashMap<>();
        newHouse = new HashMap<>();
        newTurret = new HashMap<>();
        newWall = new HashMap<>();
    }

    public void update(Entity[] entities, Map<EntityType, EntityProperties> properties) {
        oldEntities = this.entities.keySet();
        this.entities = Arrays.stream(entities).collect(HashMap::new, (m, i) -> m.put(i.getId(), i), Map::putAll);

        // Список свежих юнитов всегда будет очищаться, что бы приказы не отсылались по несколько раз
        newMapNullable();

        // Если нашли значение среди старых, то просто исключаем из списка на удаление
        // Всегда добавляем в мапы, что бы у нас были актуальные свойства entity, а не просто список
        for (Entity newEntity : entities) {
            if (oldEntities.contains(newEntity.getId())) {
                oldEntities.remove(newEntity.getId());
            } else {
                add(newEntity, true);
            }
            add(newEntity);
        }

        // Всё что не нашлось среди новых значений считаем что было убито и исключаем из мап.
        for (Integer oldEntity : oldEntities) {
            remove(oldEntity);
        }
    }

    private void add(Entity e) {
        add(e, false);
    }

    private void add(Entity e, boolean isNew) {
        switch (e.getEntityType()) {
            case BUILDER_UNIT -> {
                allBuildUnit.put(e.getId(), e);
                if (e.getPlayerId() == playerView.getMyId()) {
                    myBuildUnit.put(e.getId(), e);
                    if (isNew) {
                        newBuildUnit.put(e.getId(), e);
                    }
                }
            }
            case MELEE_UNIT -> {
                allMeleeUnit.put(e.getId(), e);
                if (e.getPlayerId() == playerView.getMyId()) {
                    myMeleeUnit.put(e.getId(), e);
                    if (isNew) {
                        newMeleeUnit.put(e.getId(), e);
                    }
                }
            }
            case RANGED_UNIT -> {
                allRangeUnit.put(e.getId(), e);
                if (e.getPlayerId() == playerView.getMyId()) {
                    myRangeUnit.put(e.getId(), e);
                    if (isNew) {
                        newRangeUnit.put(e.getId(), e);
                    }
                }
            }
            case BUILDER_BASE -> {
                allBuildBase.put(e.getId(), e);
                if (e.getPlayerId() == playerView.getMyId()) {
                    myTotalBase.put(e.getId(), e);
                    myBuildBase.put(e.getId(), e);
                    if (isNew) {
                        newBuildBase.put(e.getId(), e);
                    }
                }
            }
            case MELEE_BASE -> {
                allMeleeBase.put(e.getId(), e);
                if (e.getPlayerId() == playerView.getMyId()) {
                    myTotalBase.put(e.getId(), e);
                    myMeleeBase.put(e.getId(), e);
                    if (isNew) {
                        newMeleeBase.put(e.getId(), e);
                    }
                }
            }
            case RANGED_BASE -> {
                allRangeBase.put(e.getId(), e);
                if (e.getPlayerId() == playerView.getMyId()) {
                    myTotalBase.put(e.getId(), e);
                    myRangeBase.put(e.getId(), e);
                    if (isNew) {
                        newRangeBase.put(e.getId(), e);
                    }
                }
            }
            case HOUSE -> {
                allHouse.put(e.getId(), e);
                if (e.getPlayerId() == playerView.getMyId()) {
                    myTotalBase.put(e.getId(), e);
                    myHouse.put(e.getId(), e);
                    if (isNew) {
                        newHouse.put(e.getId(), e);
                    }
                }
            }
            case TURRET -> {
                allTurret.put(e.getId(), e);
                if (e.getPlayerId() == playerView.getMyId()) {
                    myTotalBase.put(e.getId(), e);
                    myTurret.put(e.getId(), e);
                    if (isNew) {
                        newTurret.put(e.getId(), e);
                    }
                }
            }
            case WALL -> {
                allWall.put(e.getId(), e);
                if (e.getPlayerId() == playerView.getMyId()) {
                    myWall.put(e.getId(), e);
                    if (isNew) {
                        newWall.put(e.getId(), e);
                    }
                }
            }
            case RESOURCE -> allResource.put(e.getId(), e);
        }
    }

    private void remove(Integer i) {
        allBuildUnit.remove(i);
        allMeleeUnit.remove(i);
        allRangeUnit.remove(i);
        allBuildBase.remove(i);
        allMeleeBase.remove(i);
        allRangeBase.remove(i);
        allHouse.remove(i);
        allTurret.remove(i);
        allWall.remove(i);
        allResource.remove(i);
        myBuildUnit.remove(i);
        myMeleeUnit.remove(i);
        myRangeUnit.remove(i);
        myTotalBase.remove(i);
        myBuildBase.remove(i);
        myMeleeBase.remove(i);
        myRangeBase.remove(i);
        myHouse.remove(i);
        myTurret.remove(i);
        myWall.remove(i);
    }

    private void newMapNullable() {
        newBuildUnit.clear();
        newMeleeUnit.clear();
        newRangeUnit.clear();
        newBuildBase.clear();
        newMeleeBase.clear();
        newRangeBase.clear();
        newHouse.clear();
        newTurret.clear();
        newWall.clear();
    }

    private EntityManager getEntityManager(Map<Integer, Entity> entity) {
        return new EntityManager(entity, playerView);
    }

    public EntityManager getBuild() {
        return getBuild(EntityOwner.MY);
    }

    public EntityManager getBuild(EntityOwner entityOwner) {
        return getEntityManager(switch (entityOwner) {
            case ALL -> allBuildUnit;
            case MY -> myBuildUnit;
            case NEW -> newBuildUnit;
        });
    }

    public EntityManager getMelee() {
        return getMelee(EntityOwner.MY);
    }

    public EntityManager getMelee(EntityOwner entityOwner) {
        return getEntityManager(switch (entityOwner) {
            case ALL -> allMeleeUnit;
            case MY -> myMeleeUnit;
            case NEW -> newMeleeUnit;
        });
    }

    public EntityManager getRange() {
        return getRange(EntityOwner.MY);
    }

    public EntityManager getRange(EntityOwner entityOwner) {
        return getEntityManager(switch (entityOwner) {
            case ALL -> allRangeUnit;
            case MY -> myRangeUnit;
            case NEW -> newRangeUnit;
        });
    }

    public EntityManager getTotalBase() {
        return getEntityManager(myTotalBase);
    }

    public EntityManager getBuildBase() {
        return getBuildBase(EntityOwner.MY);
    }

    public EntityManager getBuildBase(EntityOwner entityOwner) {
        return getEntityManager(switch (entityOwner) {
            case ALL -> allBuildBase;
            case MY -> myBuildBase;
            case NEW -> newBuildBase;
        });
    }

    public EntityManager getMeleeBase() {
        return getMeleeBase(EntityOwner.MY);
    }

    public EntityManager getMeleeBase(EntityOwner entityOwner) {
        return getEntityManager(switch (entityOwner) {
            case ALL -> allMeleeBase;
            case MY -> myMeleeBase;
            case NEW -> newMeleeBase;
        });
    }

    public EntityManager getRangeBase() {
        return getRangeBase(EntityOwner.MY);
    }

    public EntityManager getRangeBase(EntityOwner entityOwner) {
        return getEntityManager(switch (entityOwner) {
            case ALL -> allRangeBase;
            case MY -> myRangeBase;
            case NEW -> newRangeBase;
        });
    }

    public EntityManager getHouse() {
        return getHouse(EntityOwner.MY);
    }

    public EntityManager getHouse(EntityOwner entityOwner) {
        return getEntityManager(switch (entityOwner) {
            case ALL -> allHouse;
            case MY -> myHouse;
            case NEW -> newHouse;
        });
    }

    public EntityManager getTurret() {
        return getTurret(EntityOwner.MY);
    }

    public EntityManager getTurret(EntityOwner entityOwner) {
        return getEntityManager(switch (entityOwner) {
            case ALL -> allTurret;
            case MY -> myTurret;
            case NEW -> newTurret;
        });
    }

    public EntityManager getWall() {
        return getWall(EntityOwner.MY);
    }

    public EntityManager getWall(EntityOwner entityOwner) {
        return getEntityManager(switch (entityOwner) {
            case ALL -> allWall;
            case MY -> myWall;
            case NEW -> newWall;
        });
    }

    public EntityManager getResource() {
        return getEntityManager(allResource);
    }

    public Integer getLimit() {
        int limit = 0;
        limit += myBuildBase.size() * playerView.getEntityProperties().get(EntityType.BUILDER_BASE).getPopulationProvide();
        limit += myMeleeBase.size() * playerView.getEntityProperties().get(EntityType.MELEE_BASE).getPopulationProvide();
        limit += myRangeBase.size() * playerView.getEntityProperties().get(EntityType.RANGED_BASE).getPopulationProvide();
        limit += myHouse.size() * playerView.getEntityProperties().get(EntityType.HOUSE).getPopulationProvide();
        return limit;
    }
}
