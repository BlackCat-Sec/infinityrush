using System;
using System.Collections.Generic;

namespace InfinityRush.Runner
{
    public enum PlayerLocomotionState
    {
        Grounded,
        Jumping,
        Falling,
        Sliding,
        FastDropping
    }

    public interface IPlayerLocomotionState
    {
        PlayerLocomotionState StateType { get; }
        void Enter();
        void Exit();
        void Update(float deltaTime);
    }

    public class PlayerLocomotionStateMachine
    {
        private readonly Dictionary<PlayerLocomotionState, IPlayerLocomotionState> _states = new Dictionary<PlayerLocomotionState, IPlayerLocomotionState>();

        public IPlayerLocomotionState CurrentState { get; private set; }
        public PlayerLocomotionState CurrentStateType => CurrentState?.StateType ?? PlayerLocomotionState.Grounded;

        public event Action<PlayerLocomotionState> OnStateChanged;

        public void RegisterState(IPlayerLocomotionState state)
        {
            if (state == null) throw new ArgumentNullException(nameof(state));
            _states[state.StateType] = state;
        }

        public void ChangeState(PlayerLocomotionState newType)
        {
            if (!_states.TryGetValue(newType, out var newState))
            {
                throw new KeyNotFoundException($"Player state {newType} is not registered.");
            }

            if (CurrentState == newState)
                return;

            CurrentState?.Exit();
            CurrentState = newState;
            CurrentState.Enter();
            OnStateChanged?.Invoke(newType);
        }

        public void Update(float deltaTime)
        {
            CurrentState?.Update(deltaTime);
        }
    }
}
