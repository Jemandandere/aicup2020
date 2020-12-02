import model.Entity;
import model.PlayerView;

import java.util.ArrayList;
import java.util.List;

class RepairManager {
    private final PlayerView playerView;
    private final List<Entity> repairQueue;

    public RepairManager(PlayerView playerView) {
        this.playerView = playerView;
        this.repairQueue = new ArrayList<>();
    }

    public void update(EntityStorage entityStorage) {
        repairQueue.clear();
        for (Entity entity : entityStorage.getTotalBase().getEntity().values()) {
            if (entity.getHealth() < playerView.getEntityProperties().get(entity.getEntityType()).getMaxHealth()) {
                repairQueue.add(entity);
            }
        }
    }

    public Entity getNext() {
        return repairQueue.size() > 0 ? repairQueue.remove(0) : null;
    }

    public List<Entity> getQueue() {
        return repairQueue;
    }
}
