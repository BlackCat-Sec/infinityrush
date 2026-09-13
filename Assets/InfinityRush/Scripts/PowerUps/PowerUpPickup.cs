using UnityEngine;
using InfinityRush.Collectibles;

namespace InfinityRush.PowerUps
{
    public class PowerUpPickup : Collectible
    {
        [SerializeField] private PowerUpDefinition definition;

        public override void OnCollected(GameObject collector)
        {
            if (collector != null && collector.TryGetComponent<PowerUpController>(out var controller))
            {
                controller.ApplyPowerUp(definition);
            }
            gameObject.SetActive(false);
        }
    }
}
