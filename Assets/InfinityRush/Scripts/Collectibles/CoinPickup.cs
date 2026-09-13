using UnityEngine;
using InfinityRush.Core;
using InfinityRush.Scoring;

namespace InfinityRush.Collectibles
{
    public class CoinPickup : Collectible
    {
        [SerializeField] private int coinValue = 1;
        [SerializeField] private float rotateSpeed = 180f;

        private void Update()
        {
            transform.Rotate(Vector3.up, rotateSpeed * Time.deltaTime);
        }

        public override void OnCollected(GameObject collector)
        {
            if (ServiceRegistry.Instance.TryGet<IScoreService>(out var scoreService))
            {
                scoreService.AddCoins(coinValue);
            }
            gameObject.SetActive(false);
        }
    }
}
