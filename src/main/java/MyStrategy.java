import model.*;

public class MyStrategy {
    private GameManager gm;
    private EntityStorage e;

    public Action getAction(PlayerView playerView, DebugInterface debugInterface) {
        // Инициализируемся
        if (playerView.getCurrentTick() == 0) {
            gm = new GameManager(playerView);
            e = gm.getEntityStorage();
        }

        // Базовая стратегия внедрена в тик. Рабочие добывают, войны и лучники группируются у туррели, которая всегда стреляет.
        // После определённого этапа, разрешается строительство и соотвественно починка зданий.
        gm.tick(playerView);

        // Пора писать код

        return gm.getAction();
    }

    public void debugUpdate(PlayerView playerView, DebugInterface debugInterface) {
        debugInterface.send(new DebugCommand.Clear());
        debugInterface.getState();
    }
}