namespace InfinityRush.Core
{
    public interface IGameState
    {
        void Enter();
        void Exit();
        void Update(float deltaTime);
    }

    public abstract class GameState : IGameState
    {
        protected readonly GameStateMachine StateMachine;

        protected GameState(GameStateMachine stateMachine)
        {
            StateMachine = stateMachine;
        }

        public virtual void Enter() { }
        public virtual void Exit() { }
        public virtual void Update(float deltaTime) { }
    }

    public class BootState : GameState
    {
        public BootState(GameStateMachine stateMachine) : base(stateMachine) { }
    }

    public class MainMenuState : GameState
    {
        public MainMenuState(GameStateMachine stateMachine) : base(stateMachine) { }
    }

    public class RunnerState : GameState
    {
        public RunnerState(GameStateMachine stateMachine) : base(stateMachine) { }
    }

    public class PausedState : GameState
    {
        public PausedState(GameStateMachine stateMachine) : base(stateMachine) { }
    }

    public class GameOverState : GameState
    {
        public GameOverState(GameStateMachine stateMachine) : base(stateMachine) { }
    }
}
