using UnityEngine;

namespace InfinityRush.PowerUps
{
    [CreateAssetMenu(fileName = "PowerUpDefinition", menuName = "InfinityRush/PowerUp Definition")]
    public class PowerUpDefinition : ScriptableObject
    {
        public string id;
        public PowerUpType powerUpType;
        public string displayNameLocalizationKey;
        public Sprite icon;
        public float duration = 10.0f;
    }
}
