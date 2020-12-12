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
        // Не строить, если в шаговой доступности есть враг. +- 5 клеток.
        /* Переделать строительство, строить только там, где +нет юнитов и пытаться ТОЛЬКО если есть ресурсы

            Рабочие дрочат на месте если нет ресурсов.
            Получается, нужно из очереди строительства выдёргивать ближайшие доступные клетки, в случае, если хватает ресурсов,
            в соседней свободной клетке около уже стоящего рабочего. Пиздец.
         */


        return gm.getAction();
    }

    public void debugUpdate(PlayerView playerView, DebugInterface debugInterface) {
        debugInterface.send(new DebugCommand.Clear());
        debugInterface.getState();
    }
}