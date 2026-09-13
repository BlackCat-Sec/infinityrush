using UnityEngine;
using InfinityRush.Runner;

namespace InfinityRush.PowerUps
{
    public class JetpackEffect : IPowerUpEffect
    {
        public PowerUpType Type => PowerUpType.Jetpack;
        public bool IsActive => RemainingTime > 0f;
        public float RemainingTime { get; private set; }

        private PlayerRunnerController _controller;

        public void Apply(GameObject target, PowerUpDefinition definition)
        {
            RemainingTime = definition != null ? definition.duration : 6.0f;
            _controller = target != null ? target.GetComponent<PlayerRunnerController>() : null;

            if (_controller != null && _controller.Motor != null)
            {
                // Elevate player
            }
        }

        public void Update(float deltaTime)
        {
            if (!IsActive) return;

            RemainingTime -= deltaTime;
            if (RemainingTime <= 0f)
            {
                Cancel();
            }
        }

        public void Cancel()
        {
            RemainingTime = 0f;
            _controller = null;
        }
    }
}
