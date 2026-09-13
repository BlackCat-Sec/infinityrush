using UnityEngine;

namespace InfinityRush.PowerUps
{
    public interface IPowerUpEffect
    {
        PowerUpType Type { get; }
        bool IsActive { get; }
        float RemainingTime { get; }

        void Apply(GameObject target, PowerUpDefinition definition);
        void Update(float deltaTime);
        void Cancel();
    }
}
