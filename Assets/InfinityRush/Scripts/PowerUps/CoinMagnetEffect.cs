using UnityEngine;
using InfinityRush.Collectibles;

namespace InfinityRush.PowerUps
{
    public class CoinMagnetEffect : IPowerUpEffect
    {
        public PowerUpType Type => PowerUpType.CoinMagnet;
        public bool IsActive => RemainingTime > 0f;
        public float RemainingTime { get; private set; }

        private GameObject _target;
        private float _pullRadius = 12.0f;
        private float _pullSpeed = 22.0f;

        public void Apply(GameObject target, PowerUpDefinition definition)
        {
            _target = target;
            RemainingTime = definition != null ? definition.duration : 10.0f;
        }

        public void Update(float deltaTime)
        {
            if (!IsActive || _target == null) return;

            RemainingTime -= deltaTime;

            // Find nearby coins and attract them
            var coins = Object.FindObjectsOfType<CoinPickup>();
            Vector3 playerPos = _target.transform.position;

            foreach (var coin in coins)
            {
                if (coin.gameObject.activeInHierarchy)
                {
                    float dist = Vector3.Distance(playerPos, coin.transform.position);
                    if (dist <= _pullRadius)
                    {
                        coin.transform.position = Vector3.MoveTowards(
                            coin.transform.position,
                            playerPos,
                            _pullSpeed * deltaTime
                        );

                        if (dist <= 0.8f)
                        {
                            coin.OnCollected(_target);
                        }
                    }
                }
            }
        }

        public void Cancel()
        {
            RemainingTime = 0f;
            _target = null;
        }
    }
}
