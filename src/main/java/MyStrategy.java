import model.*;

import java.util.*;

class Tools{
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

    public static Vec2Int getMinimalPos(Vec2Int bPos, List<Vec2Int> availablePos) {
        double minDistance = 99;
        double curDistance = 0;
        Vec2Int minPos = new Vec2Int();
        for (Vec2Int aPos : availablePos) {
            curDistance = getSqrtDistance(aPos, bPos);
            if (minDistance > curDistance) {
                minDistance = curDistance;
                minPos = aPos;
            }
        }
        return minPos;
    }

    public static List<Vec2Int> getAvailablePos(Building toBuild, PlayerView playerView) {
        List<Vec2Int> availablePos = new ArrayList<>();
        int buildSize = playerView.getEntityProperties().get(toBuild.getType()).getSize();
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
        // TODO Отсеевать значения точек, на которых уже построены здания. Вероятнее всего не понадобится
        return availablePos;
    }
}

class GameMap {
    private boolean[][] map;


    public GameMap() {
        this(80);
    }

    public GameMap(Integer size) {
        this(size, size);
    }

    public GameMap(Integer width, Integer heigth) {
        this.map = new boolean[width][heigth];
        // TODO Проверить правильность размещения координат, по идее должно быть так.
    }

    public void clear() {
        for (boolean[] row : map) {
            for (boolean cell : row) {
                cell = false;
            }
        }
    }

    public void print() {
        for (boolean[] row : map) {
            for (boolean cell : row) {
                System.out.println(cell ? 1 : 0);
            }
        }
    }

    public void refresh(Entity[] entities, Map<EntityType, EntityProperties> properties) {
        // TODO Написать обновление карты
    }
}

class Limits {
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

enum EntityOwner {
    ALL(-1),
    MY(0),
    NEW(1);
    public int tag;
    EntityOwner(int tag) {
        this.tag = tag;
    }
}

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
        return attack(new AutoAttack(1000, validTargets)); //TODO Вот тут плохо реализован рэнж
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
        return build(null);
    }
    public EntityManager build(Vec2Int position) {
        return build(null, position);
    }
    // Это для строителей

    public EntityManager build(EntityType entityType, Vec2Int position) {
        this.build = true;
        this.buildEntityType = entityType;
        this.buildPosition = position;
        return this;
    }

    // Сделать починку

    public Map<Integer, EntityAction> make(){
        Map<Integer, EntityAction> actions = new HashMap<>();
        for (Map.Entry<Integer, Entity> entry : entity.entrySet()) {
            // TODO To, что сделано с билдом нужно сделать для остальных т.к. для атаки есть entityProperties.getSightRange()
            if (build) {
                if (buildEntityType == null) {
                    buildEntityType = playerView.getEntityProperties().get(entry.getValue().getEntityType()).getBuild().getOptions()[0];
                    //Integer currentUnits = Math.toIntExact(Arrays.stream(playerView.getEntities()).filter(e -> gm.getId().equals(e.getPlayerId()) & e.getEntityType() == entityType).count());
                    //if ((currentUnits + 1) * playerView.getEntityProperties().get(entityType).getPopulationUse() <= limits.get(currentLimit).getLimit(entityType)) {
                }
                if (buildPosition == null) {
                    buildPosition =  new Vec2Int(
                            entry.getValue().getPosition().getX() + playerView.getEntityProperties().get(entry.getValue().getEntityType()).getSize(),
                            entry.getValue().getPosition().getY() + playerView.getEntityProperties().get(entry.getValue().getEntityType()).getSize() - 1
                    );
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

    private void add(Entity e){
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
                break;
            }
            case RANGED_UNIT -> {
                allRangeUnit.put(e.getId(), e);
                if (e.getPlayerId() == playerView.getMyId()) {
                    myRangeUnit.put(e.getId(), e);
                    if (isNew) {
                        newRangeUnit.put(e.getId(), e);
                    }
                }
                break;
            }
            case BUILDER_BASE -> {
                allBuildBase.put(e.getId(), e);
                if (e.getPlayerId() == playerView.getMyId()) {
                    myBuildBase.put(e.getId(), e);
                    if (isNew) {
                        newBuildBase.put(e.getId(), e);
                    }
                }
                break;
            }
            case MELEE_BASE -> {
                allMeleeBase.put(e.getId(), e);
                if (e.getPlayerId() == playerView.getMyId()) {
                    myMeleeBase.put(e.getId(), e);
                    if (isNew) {
                        newMeleeBase.put(e.getId(), e);
                    }
                }
                break;
            }
            case RANGED_BASE -> {
                allRangeBase.put(e.getId(), e);
                if (e.getPlayerId() == playerView.getMyId()) {
                    myRangeBase.put(e.getId(), e);
                    if (isNew) {
                        newRangeBase.put(e.getId(), e);
                    }
                }
                break;
            }
            case HOUSE -> {
                allHouse.put(e.getId(), e);
                if (e.getPlayerId() == playerView.getMyId()) {
                    myHouse.put(e.getId(), e);
                    if (isNew) {
                        newHouse.put(e.getId(), e);
                    }
                }
                break;
            }
            case TURRET -> {
                allTurret.put(e.getId(), e);
                if (e.getPlayerId() == playerView.getMyId()) {
                    myTurret.put(e.getId(), e);
                    if (isNew) {
                        newTurret.put(e.getId(), e);
                    }
                }
                break;
            }
            case WALL -> {
                allWall.put(e.getId(), e);
                if (e.getPlayerId() == playerView.getMyId()) {
                    myWall.put(e.getId(), e);
                    if (isNew) {
                        newWall.put(e.getId(), e);
                    }
                }
                break;
            }
            case RESOURCE -> {
                allResource.put(e.getId(), e);
            }
        }
        // TODO Доделать для всех юнитов
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
        return getEntityManager(switch (entityOwner){
            case ALL -> allBuildUnit;
            case MY -> myBuildUnit;
            case NEW -> newBuildUnit;
        });
    }

    public EntityManager getMelee() {
        return getMelee(EntityOwner.MY);
    }
    public EntityManager getMelee(EntityOwner entityOwner) {
        return getEntityManager(switch (entityOwner){
            case ALL -> allMeleeUnit;
            case MY -> myMeleeUnit;
            case NEW -> newMeleeUnit;
        });
    }

    public EntityManager getRange() {
        return getRange(EntityOwner.MY);
    }
    public EntityManager getRange(EntityOwner entityOwner) {
        return getEntityManager(switch (entityOwner){
            case ALL -> allRangeUnit;
            case MY -> myRangeUnit;
            case NEW -> newRangeUnit;
        });
    }

    public EntityManager getBuildBase() {
        return getBuildBase(EntityOwner.MY);
    }
    public EntityManager getBuildBase(EntityOwner entityOwner) {
        return getEntityManager(switch (entityOwner){
            case ALL -> allBuildBase;
            case MY -> myBuildBase;
            case NEW -> newBuildBase;
        });
    }

    public EntityManager getMeleeBase() {
        return getMeleeBase(EntityOwner.MY);
    }
    public EntityManager getMeleeBase(EntityOwner entityOwner) {
        return getEntityManager(switch (entityOwner){
            case ALL -> allMeleeBase;
            case MY -> myMeleeBase;
            case NEW -> newMeleeBase;
        });
    }

    public EntityManager getRangeBase() {
        return getRangeBase(EntityOwner.MY);
    }
    public EntityManager getRangeBase(EntityOwner entityOwner) {
        return getEntityManager(switch (entityOwner){
            case ALL -> allRangeBase;
            case MY -> myRangeBase;
            case NEW -> newRangeBase;
        });
    }

    public EntityManager getHouse() {
        return getHouse(EntityOwner.MY);
    }
    public EntityManager getHouse(EntityOwner entityOwner) {
        return getEntityManager(switch (entityOwner){
            case ALL -> allHouse;
            case MY -> myHouse;
            case NEW -> newHouse;
        });
    }

    public EntityManager getTurret() {
        return getTurret(EntityOwner.MY);
    }
    public EntityManager getTurret(EntityOwner entityOwner) {
        return getEntityManager(switch (entityOwner){
            case ALL -> allTurret;
            case MY -> myTurret;
            case NEW -> newTurret;
        });
    }

    public EntityManager getWall() {
        return getWall(EntityOwner.MY);
    }
    public EntityManager getWall(EntityOwner entityOwner) {
        return getEntityManager(switch (entityOwner){
            case ALL -> allWall;
            case MY -> myWall;
            case NEW -> newWall;
        });
    }

    public EntityManager getResource() {
        return getEntityManager(allResource);
    }

    public Integer getLimit() {
        Integer limit = 0;
        limit += myBuildBase.size() * playerView.getEntityProperties().get(EntityType.BUILDER_BASE).getPopulationProvide();
        limit += myMeleeBase.size() * playerView.getEntityProperties().get(EntityType.MELEE_BASE).getPopulationProvide();
        limit += myRangeBase.size() * playerView.getEntityProperties().get(EntityType.RANGED_BASE).getPopulationProvide();
        limit += myHouse.size() * playerView.getEntityProperties().get(EntityType.HOUSE).getPopulationProvide();
        return limit;
    }
}

class Building {
    private Integer id;
    private Vec2Int pos;
    private EntityType type;
    private Entity builded;

    public Building(Vec2Int pos, EntityType type) {
        this.pos = pos;
        this.type = type;
        this.builded = null;
    }

    public Vec2Int getPos() {
        return pos;
    }

    public EntityType getType() {
        return type;
    }

    public Entity getBuilded() {
        return builded;
    }

    public void setBuilded(Entity builded) {
        this.id = builded.getId();
        this.builded = builded;
    }
}

class GameManager {
    // Наш id
    private Integer id;
    // Карта
    private GameMap map;
    // Свойства всех юнитов
    private Map<EntityType, EntityProperties> properties;
    // Хранилище всех юнитов
    private EntityStorage entityStorage;
    // Максимальный лимит
    private Integer limit;
    // Пороговые лимиты для каждого типа юнитов
    private Map<Integer, Limits> limits;
    // Действия передаваемые на сервер
    private Map<Integer, EntityAction> actions;

    public GameManager(PlayerView playerView) {
        id = playerView.getMyId();
        map = new GameMap();
        properties = playerView.getEntityProperties();
        entityStorage = new EntityStorage(playerView);
        actions = new HashMap<>();
        limits = new HashMap<>();
        // Зададим лимиты
        limits.put(0, new Limits(0,0,0));
        limits.put(5, new Limits(5,0,0));
        limits.put(10, new Limits(10,0,0));
        limits.put(15, new Limits(10,0,5));
        limits.put(20, new Limits(15,0,5));
        limits.put(25, new Limits(15,0,10));
        limits.put(30, new Limits(20,0,10));
        limits.put(35, new Limits(20,0,15));
        limits.put(40, new Limits(25,0,15));
        limits.put(45, new Limits(25,0,20));
        limits.put(50, new Limits(30,0,20));
        limits.put(55, new Limits(30,0,25));
        limits.put(60, new Limits(35,0,25));
        limits.put(65, new Limits(35,0,30));
        limits.put(70, new Limits(35,0,35));
        limits.put(75, new Limits(35,0,40));
        limits.put(80, new Limits(35,0,45));
        limits.put(85, new Limits(35,0,50));
        limits.put(90, new Limits(35,0,55));
        limits.put(95, new Limits(35,0,60));
        limits.put(100, new Limits(40,0,60));
        limits.put(105, new Limits(40,0,65));
        limits.put(110, new Limits(40,0,70));
        limits.put(115, new Limits(40,0,75));
        limits.put(120, new Limits(40,0,80));
        limits.put(125, new Limits(40,0,85));
        limits.put(130, new Limits(40,0,90));
        limits.put(135, new Limits(40,0,95));
        limits.put(140, new Limits(40,0,100));
        limits.put(145, new Limits(40,0,105));
        limits.put(150, new Limits(40,0,110));
        limits.put(155, new Limits(40,0,115));
        limits.put(160, new Limits(40,0,120));
        limits.put(165, new Limits(40,0,125));
        limits.put(170, new Limits(40,0,130));
        limits.put(175, new Limits(40,0,135));
        limits.put(180, new Limits(40,0,140));
        limits.put(185, new Limits(40,0,145));
        limits.put(190, new Limits(40,0,150));
        limits.put(195, new Limits(40,0,155));
        limits.put(200, new Limits(40,0,160));


    }

    public Integer getId() {
        return id;
    }

    public GameMap getMap() {
        return map;
    }

    public Map<EntityType, EntityProperties> getProperties() {
        return properties;
    }

    public EntityProperties getProperty(EntityType entityType) {
        return properties.get(entityType);
    }

    public EntityStorage getEntityStorage() {
        return entityStorage;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getLimit(EntityType entityType) {
        return limits.get(limit).getLimit(entityType);
    }

    public boolean canSpawnBuildUnit () {
        // TODO При увеличении количества строений, могут быть ложные срабатывания
        return entityStorage.getBuild().count() < getLimit(EntityType.BUILDER_UNIT);
    }

    public boolean canSpawnMeleeUnit () {
        return entityStorage.getMelee().count() < getLimit(EntityType.MELEE_UNIT);
    }

    public boolean canSpawnRangeUnit () {
        return entityStorage.getRange().count() < getLimit(EntityType.RANGED_UNIT);
    }

    public void tick(PlayerView playerView) {
        System.out.println("-----" + playerView.getCurrentTick() + "-----");
        map.refresh(playerView.getEntities(), properties);
        entityStorage.update(playerView.getEntities(), properties);
        limit = entityStorage.getLimit();
        basicStrategy();
    }

    public void basicStrategy () {
        // TODO Добавить определение раунда игры
        // Обнуляем старые действия
        actions.clear();
        // Рабочие постоянно добывают ресурсы, по всей карте
        putActions(getEntityStorage().getBuild().attack(new EntityType[]{EntityType.RESOURCE}).make());
        // Милишник на данный момент времени бесполезен, поэтому пойдет на базу к вражине
        putActions(getEntityStorage().getMelee().move(75, 5).make());
        // Рэнжи группируются
        putActions(getEntityStorage().getRange().move(14, 14).make());

        // Базы строят если могут
        if (canSpawnBuildUnit()) {
            putActions(getEntityStorage().getBuildBase().build().make());
        } else {
            putActions(getEntityStorage().getBuildBase().make());
        }
        if (canSpawnRangeUnit()) {
            putActions(getEntityStorage().getRangeBase().build().make());
        } else {
            putActions(getEntityStorage().getRangeBase().make());
        }

        // Туррели постоянно атакуют //
        putActions(getEntityStorage().getTurret().attack().make());
    }

    public void putActions(Map<Integer, EntityAction> actions) {
        this.actions.putAll(actions);
    }

    public Action getAction() {
        return new Action(actions);
    }
}

public class MyStrategy {

    private GameManager gm;
    private EntityStorage e;


    private static Map<Integer, Integer> craftUnitLimits = new HashMap<Integer, Integer>();
    private List<Building> buildQueue = new ArrayList<>();
    private static Map<Integer, Building> buildTasks = new HashMap<>();

    private void init() {
        craftUnitLimits.put(0, 0);
        craftUnitLimits.put(1, 0);
        craftUnitLimits.put(2, 0);
        craftUnitLimits.put(3, 0);
        craftUnitLimits.put(4, 0);
        craftUnitLimits.put(5, 0);
        craftUnitLimits.put(6, 0);
        craftUnitLimits.put(7, 0);
        craftUnitLimits.put(8, 0);
        craftUnitLimits.put(9, 0);
        craftUnitLimits.put(10, 1);
        craftUnitLimits.put(11, 1);
        craftUnitLimits.put(12, 1);
        craftUnitLimits.put(13, 1);
        craftUnitLimits.put(14, 1);
        craftUnitLimits.put(15, 2);
        craftUnitLimits.put(16, 2);
        craftUnitLimits.put(17, 2);
        craftUnitLimits.put(18, 2);
        craftUnitLimits.put(19, 2);
        craftUnitLimits.put(20, 3);
        craftUnitLimits.put(21, 3);
        craftUnitLimits.put(22, 3);
        craftUnitLimits.put(23, 3);
        craftUnitLimits.put(24, 3);
        craftUnitLimits.put(25, 4);
        craftUnitLimits.put(26, 4);
        craftUnitLimits.put(27, 4);
        craftUnitLimits.put(28, 4);
        craftUnitLimits.put(29, 4);
        craftUnitLimits.put(30, 5);
        craftUnitLimits.put(31, 5);
        craftUnitLimits.put(32, 5);
        craftUnitLimits.put(33, 5);
        craftUnitLimits.put(34, 5);
        craftUnitLimits.put(35, 5);
        craftUnitLimits.put(36, 5);
        craftUnitLimits.put(37, 5);
        craftUnitLimits.put(38, 5);
        craftUnitLimits.put(39, 5);
        craftUnitLimits.put(40, 5);

        buildQueue.add(new Building(new Vec2Int(1, 1), EntityType.HOUSE));
        buildQueue.add(new Building(new Vec2Int(5, 1), EntityType.HOUSE));
        buildQueue.add(new Building(new Vec2Int(1, 5), EntityType.HOUSE));
        buildQueue.add(new Building(new Vec2Int(9, 1), EntityType.HOUSE));
        buildQueue.add(new Building(new Vec2Int(1, 9), EntityType.HOUSE));

        buildQueue.add(new Building(new Vec2Int(11, 6), EntityType.HOUSE));

        buildQueue.add(new Building(new Vec2Int(13, 1), EntityType.HOUSE));
        buildQueue.add(new Building(new Vec2Int(17, 1), EntityType.HOUSE));
        buildQueue.add(new Building(new Vec2Int(21, 1), EntityType.HOUSE));
        buildQueue.add(new Building(new Vec2Int(21, 5), EntityType.HOUSE));
        buildQueue.add(new Building(new Vec2Int(21, 9), EntityType.HOUSE));

        buildQueue.add(new Building(new Vec2Int(5, 11), EntityType.HOUSE));
        buildQueue.add(new Building(new Vec2Int(9, 11), EntityType.HOUSE));
        buildQueue.add(new Building(new Vec2Int(13, 11), EntityType.HOUSE));
        buildQueue.add(new Building(new Vec2Int(17, 11), EntityType.HOUSE));
        buildQueue.add(new Building(new Vec2Int(21, 13), EntityType.HOUSE));

        buildQueue.add(new Building(new Vec2Int(25, 1), EntityType.HOUSE));
        buildQueue.add(new Building(new Vec2Int(25, 5), EntityType.HOUSE));
        buildQueue.add(new Building(new Vec2Int(25, 9), EntityType.HOUSE));
        buildQueue.add(new Building(new Vec2Int(25, 13), EntityType.HOUSE));

        buildQueue.add(new Building(new Vec2Int(29, 1), EntityType.HOUSE));
        buildQueue.add(new Building(new Vec2Int(29, 5), EntityType.HOUSE));
        buildQueue.add(new Building(new Vec2Int(29, 9), EntityType.HOUSE));
        buildQueue.add(new Building(new Vec2Int(29, 13), EntityType.HOUSE));
        // TODO Нужно организовать проверку по доступным точкам для строительства, мол, если там можем построить, что бы строило, что бы не ждать
    }

    public Action getAction(PlayerView playerView, DebugInterface debugInterface) {

        // Инициализируемся
        if (playerView.getCurrentTick() == 0) {
            gm = new GameManager(playerView);
            e = gm.getEntityStorage();
        }

        gm.tick(playerView);


        /*

        // Рабочие вседа добывают, но будет пачка, которая строит
        for (Integer i : Ent.builderUnits.keySet()) {
            Entity entity = Ent.builderUnits.get(i);

            if (Ent.craftUnits.size() < craftUnitLimits.get(Ent.builderUnits.size())) {
                Ent.craftUnits.put(entity.getId(), entity);
            }

            if (Ent.craftUnits.containsKey(entity.getId())) {
                // Перечитаем строителя, иначе он запоминает состояние на момен появления (координаты тоже) и в поисках ближайшей точки строительства нет смысла
                Ent.craftUnits.put(entity.getId(), entity);
                if (buildQueue.iterator().hasNext()) {
                    if (buildTasks.get(entity.getId()) == null) {
                        buildTasks.put(entity.getId(), buildQueue.remove(0));
                    }
                }
                if (buildTasks.get(entity.getId()) != null) {
                    if (buildTasks.get(entity.getId()).getBuilded() == null) {
                        for (Entity building : Ent.totalBuildings.values()) {
                            if (Tools.equalVec(building.getPosition(), buildTasks.get(entity.getId()).getPos())) {
                                // Считаем что такое здание было построено
                                buildTasks.get(entity.getId()).setBuilded(building);
                            }
                        }
                        m = new MoveAction(Tools.getMinimalPos(entity.getPosition(), Tools.getAvailablePos(buildTasks.get(entity.getId()), playerView)), true, true);
                        b = new BuildAction(buildTasks.get(entity.getId()).getType(), buildTasks.get(entity.getId()).getPos());
                    }
                    if (buildTasks.get(entity.getId()).getBuilded() != null) {
                        if (Ent.totalBuildings.get(buildTasks.get(entity.getId()).getBuilded().getId()).isActive()) {
                            buildTasks.remove(entity.getId()); // TODO Если юнита убьют, задание пропадёт? Нужно поправить.
                        } else {
                            r = new RepairAction(buildTasks.get(entity.getId()).getBuilded().getId());
                        }
                    }
                } else {
                    m = new MoveAction(new Vec2Int(playerView.getMapSize() / 2 - 1, playerView.getMapSize() / 2 - 1), true, false);
                    a = new AttackAction(
                            null,
                            new AutoAttack(
                                    playerView.getMapSize(),
                                    new EntityType[]{EntityType.RESOURCE}
                            )
                    );
                }
            }
        }

         */
        return gm.getAction();
    }

    public void debugUpdate(PlayerView playerView, DebugInterface debugInterface) {
        debugInterface.send(new DebugCommand.Clear());
        debugInterface.getState();
    }
}