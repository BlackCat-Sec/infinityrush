using System;
using System.Collections.Generic;

namespace InfinityRush.Core
{
    public class GameStateMachine
    {
        private readonly Dictionary<Type, IGameState> _states = new Dictionary<Type, IGameState>();
        public IGameState CurrentState { get; private set; }

        public void RegisterState(IGameState state)
        {
            if (state == null)
                throw new ArgumentNullException(nameof(state));

            _states[state.GetType()] = state;
        }

        public void ChangeState<T>() where T : IGameState
        {
            var type = typeof(T);
            if (!_states.TryGetValue(type, out var newState))
            {
                throw new KeyNotFoundException($"State of type {type.Name} is not registered.");
            }

            CurrentState?.Exit();
            CurrentState = newState;
            CurrentState.Enter();
        }

        public void Update(float deltaTime)
        {
            CurrentState?.Update(deltaTime);
        }
    }
}
