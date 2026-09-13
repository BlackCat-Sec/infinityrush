using UnityEngine;
using InfinityRush.Core;
using InfinityRush.Scoring;

namespace InfinityRush.PowerUps
{
    public class ScoreMultiplierEffect : IPowerUpEffect
    {
        public PowerUpType Type => PowerUpType.ScoreMultiplier;
        public bool IsActive => RemainingTime > 0f;
        public float RemainingTime { get; private set; }

        private IScoreService _scoreService;

        public void Apply(GameObject target, PowerUpDefinition definition)
        {
            RemainingTime = definition != null ? definition.duration : 12.0f;

            if (ServiceRegistry.Instance.TryGet<IScoreService>(out _scoreService))
            {
                _scoreService.SetMultiplier(2);
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
            if (_scoreService != null)
            {
                _scoreService.ResetMultiplier();
                _scoreService = null;
            }
        }
    }
}
