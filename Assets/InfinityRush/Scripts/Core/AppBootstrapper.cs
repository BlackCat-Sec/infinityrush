using UnityEngine;

namespace InfinityRush.Core
{
    public class AppBootstrapper : MonoBehaviour
    {
        [SerializeField] private int targetFrameRate = 60;

        public GameStateMachine StateMachine { get; private set; }

        private void Awake()
        {
            Application.targetFrameRate = targetFrameRate;

            ServiceRegistry.Instance.Register(this);

            StateMachine = new GameStateMachine();
            StateMachine.RegisterState(new BootState(StateMachine));
            StateMachine.RegisterState(new MainMenuState(StateMachine));
            StateMachine.RegisterState(new RunnerState(StateMachine));
            StateMachine.RegisterState(new PausedState(StateMachine));
            StateMachine.RegisterState(new GameOverState(StateMachine));

            ServiceRegistry.Instance.Register(StateMachine);

            StateMachine.ChangeState<BootState>();
        }

        private void Update()
        {
            StateMachine?.Update(Time.deltaTime);
        }

        private void OnDestroy()
        {
            ServiceRegistry.Instance.Unregister<AppBootstrapper>();
            ServiceRegistry.Instance.Unregister<GameStateMachine>();
        }
    }
}
