import model.*;

import java.util.*;

public class GameManager {
    // Наш id
    private Integer id;
    // Формат игры
    private GameFormat gameFormat;
    // Фаза игры
    private GamePhase gamePhase;
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
    // Менеджер построек
    private BuildingManager buildingManager;
    // Менеджер ремонта
    private RepairManager repairManager;
    // Действия передаваемые на сервер
    private Map<Integer, EntityAction> actions;

    public GameManager(PlayerView playerView) {
        id = playerView.getMyId();
        gameFormat = !playerView.isFogOfWar() ? GameFormat.ROUND1 : playerView.getPlayers().length == 4 ? GameFormat.ROUND2 : GameFormat.FINAL;
        gamePhase = GamePhase.START;
        map = new GameMap(playerView);
        properties = playerView.getEntityProperties();
        entityStorage = new EntityStorage(playerView);
        limits = getLimitsMap();
        buildingManager = new BuildingManager(getBuildingList(), playerView);
        repairManager = new RepairManager(playerView);
        actions = new HashMap<>();
    }

    private Map<Integer, Limits> getLimitsMap() {
        Map <Integer, Limits> limitsMap = new HashMap<>();

        // Зададим лимиты
        limitsMap.put(0, new Limits(0,0,0));
        limitsMap.put(5, new Limits(5,0,0));
        limitsMap.put(10, new Limits(10,0,0));
        limitsMap.put(15, new Limits(13,1,1));
        limitsMap.put(20, new Limits(18,1,1));
        limitsMap.put(25, new Limits(18,1,6));
        limitsMap.put(30, new Limits(20,0,10));
        limitsMap.put(35, new Limits(20,0,15));
        limitsMap.put(40, new Limits(25,0,15));
        limitsMap.put(45, new Limits(25,0,20));
        limitsMap.put(50, new Limits(30,0,20));
        limitsMap.put(55, new Limits(30,0,25));
        limitsMap.put(60, new Limits(35,0,25));
        limitsMap.put(65, new Limits(35,0,30));
        limitsMap.put(70, new Limits(35,0,35));
        limitsMap.put(75, new Limits(35,0,40));
        limitsMap.put(80, new Limits(35,0,45));
        limitsMap.put(85, new Limits(35,0,50));
        limitsMap.put(90, new Limits(35,0,55));
        limitsMap.put(95, new Limits(35,0,60));
        limitsMap.put(100, new Limits(40,0,60));
        limitsMap.put(105, new Limits(40,0,65));
        limitsMap.put(110, new Limits(40,0,70));
        limitsMap.put(115, new Limits(40,0,75));
        limitsMap.put(120, new Limits(40,0,80));
        limitsMap.put(125, new Limits(40,0,85));
        limitsMap.put(130, new Limits(40,0,90));
        limitsMap.put(135, new Limits(40,0,95));
        limitsMap.put(140, new Limits(40,0,100));
        limitsMap.put(145, new Limits(40,0,105));
        limitsMap.put(150, new Limits(40,0,110));
        limitsMap.put(155, new Limits(40,0,115));
        limitsMap.put(160, new Limits(40,0,120));
        limitsMap.put(165, new Limits(40,0,125));
        limitsMap.put(170, new Limits(40,0,130));
        limitsMap.put(175, new Limits(40,0,135));
        limitsMap.put(180, new Limits(40,0,140));
        limitsMap.put(185, new Limits(40,0,145));
        limitsMap.put(190, new Limits(40,0,150));
        limitsMap.put(195, new Limits(40,0,155));
        limitsMap.put(200, new Limits(40,0,160));

        return limitsMap;
    }

    private List<Building> getBuildingList() {
        List<Building> buildingList = new ArrayList<>();

        buildingList.add(new Building(new Vec2Int(1, 1), EntityType.HOUSE));

        buildingList.add(new Building(new Vec2Int(5, 1), EntityType.HOUSE));
        buildingList.add(new Building(new Vec2Int(1, 5), EntityType.HOUSE));
        buildingList.add(new Building(new Vec2Int(9, 1), EntityType.HOUSE));
        buildingList.add(new Building(new Vec2Int(1, 9), EntityType.HOUSE));

        buildingList.add(new Building(new Vec2Int(11, 6), EntityType.HOUSE));

        buildingList.add(new Building(new Vec2Int(13, 1), EntityType.HOUSE));
        buildingList.add(new Building(new Vec2Int(17, 1), EntityType.HOUSE));
        buildingList.add(new Building(new Vec2Int(21, 1), EntityType.HOUSE));
        buildingList.add(new Building(new Vec2Int(21, 5), EntityType.HOUSE));
        buildingList.add(new Building(new Vec2Int(21, 9), EntityType.HOUSE));

        buildingList.add(new Building(new Vec2Int(5, 11), EntityType.HOUSE));
        buildingList.add(new Building(new Vec2Int(9, 11), EntityType.HOUSE));
        buildingList.add(new Building(new Vec2Int(13, 11), EntityType.HOUSE));
        buildingList.add(new Building(new Vec2Int(17, 11), EntityType.HOUSE));
        buildingList.add(new Building(new Vec2Int(21, 13), EntityType.HOUSE));

        buildingList.add(new Building(new Vec2Int(25, 1), EntityType.HOUSE));
        buildingList.add(new Building(new Vec2Int(25, 5), EntityType.HOUSE));
        buildingList.add(new Building(new Vec2Int(25, 9), EntityType.HOUSE));
        buildingList.add(new Building(new Vec2Int(25, 13), EntityType.HOUSE));

        buildingList.add(new Building(new Vec2Int(29, 1), EntityType.HOUSE));
        buildingList.add(new Building(new Vec2Int(29, 5), EntityType.HOUSE));
        buildingList.add(new Building(new Vec2Int(29, 9), EntityType.HOUSE));
        buildingList.add(new Building(new Vec2Int(29, 13), EntityType.HOUSE));
        return buildingList;
    }

    public Integer getId() {
        return id;
    }

    public GameFormat getGameFormat() {
        return gameFormat;
    }

    public GamePhase getGamePhase() {
        return gamePhase;
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

    public Integer getCrafterLimit(Integer buildersCount) {
        return buildersCount / 5;
    }
    public BuildingManager getBuildingManager() {
        return buildingManager;
    }

    public RepairManager getRepairManager() {
        return repairManager;
    }

    private GamePhase updateGamePhase() {
        GamePhase phase = getGamePhase();
        if (phase == GamePhase.START) {
            if (getGameFormat() == GameFormat.ROUND1) {
                if (getEntityStorage().getBuild().count() >= 13) {
                    phase = GamePhase.ALLOW_BUILD;
                }
            } else {
                if (getEntityStorage().getBuild().count() >= 5) {
                    phase = GamePhase.ALLOW_BUILD;
                }
            }
        }
        return phase;
    }

    public void tick(PlayerView playerView) {
        System.out.println("-----" + playerView.getCurrentTick() + "-----");
        map.refresh(playerView.getEntities(), properties);
        entityStorage.update(playerView.getEntities(), properties);
        limit = entityStorage.getLimit();
        buildingManager.update(getMap());
        repairManager.update(getEntityStorage());
        gamePhase = updateGamePhase();
        basicStrategy();
    }

    // При увеличении количества строений, могут быть ложные срабатывания
    public boolean canSpawnBuildUnit () {
        return entityStorage.getBuild().count() < getLimit(EntityType.BUILDER_UNIT);
    }
    // При увеличении количества строений, могут быть ложные срабатывания
    public boolean canSpawnMeleeUnit () {
        return entityStorage.getMelee().count() < getLimit(EntityType.MELEE_UNIT);
    }
    // При увеличении количества строений, могут быть ложные срабатывания
    public boolean canSpawnRangeUnit () {
        return entityStorage.getRange().count() < getLimit(EntityType.RANGED_UNIT);
    }

    public void basicStrategy () {
        // Обнуляем старые действия
        actions.clear();
        // Рабочие постоянно добывают ресурсы, по всей карте
        putActions(getEntityStorage().getBuild().attack(new EntityType[]{EntityType.RESOURCE}).make());
        // Милишник на данный момент времени бесполезен, поэтому пойдет на базу к вражине
        putActions(getEntityStorage().getMelee().move(75, 5).attack().make());
        // Рэнжи группируются
        putActions(getEntityStorage().getRange().move(map.getSize() / 2, map.getSize() / 2).attack().make());

        if (getGamePhase().tag >= GamePhase.ALLOW_BUILD.tag) {
            // Стройка
            Set<Integer> builders = new HashSet<>();
            for (int i = 0; i < getCrafterLimit(getEntityStorage().getBuild().count()); i++) {
                Building building = getBuildingManager().getNext();
                if (building != null) {
                    EntityManager entityManager = getEntityStorage().getBuild().filter(builders).getNearest(building.getPos(), getProperty(building.getType()).getSize(), true);
                    builders.addAll(entityManager.getEntity().keySet());
                    putActions(entityManager.build(building).make());
                }
            }
            // Ремонт
            Set<Integer> repairs = new HashSet<>();
            for (Entity repair : getRepairManager().getQueue()) {
                EntityManager entityManager = getEntityStorage().getBuild().getNearest(repair.getPosition(), getProperty(repair.getEntityType()).getSize());
                repairs.addAll(entityManager.getEntity().keySet());
                putActions(entityManager.repair(repair.getId()).make());
            }
        }

        // Базы строят если могут
        if (canSpawnBuildUnit()) {
            putActions(getEntityStorage().getBuildBase().build().make());
        } else {
            putActions(getEntityStorage().getBuildBase().make());
        }
        if (canSpawnMeleeUnit()) {
            putActions(getEntityStorage().getMeleeBase().build().make());
        } else {
            putActions(getEntityStorage().getMeleeBase().make());
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

