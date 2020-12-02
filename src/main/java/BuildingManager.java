import model.PlayerView;

import java.util.ArrayList;
import java.util.List;

class BuildingManager {
    private PlayerView playerView;
    private List<Building> buildingList; // Список всех построек
    private List<Building> buildingQueue; // Список возможных построек


    public BuildingManager(List<Building> buildingList, PlayerView playerView) {
        this.playerView = playerView;
        this.buildingList = buildingList;
        this.buildingQueue = new ArrayList<>();
    }

    public void update(GameMap gameMap) {
        buildingQueue.clear();
        for (Building building : buildingList) {
            if (gameMap.isFree(building.getPos(), playerView.getEntityProperties().get(building.getType()).getSize())) {
                buildingQueue.add(building);
            }
        }
    }

    public Building getNext() {
        return buildingQueue.size() > 0 ? buildingQueue.remove(0) : null;
    }

    public List<Building> getQueue() {
        return buildingQueue;
    }
}
