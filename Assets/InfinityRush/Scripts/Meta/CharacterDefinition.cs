using UnityEngine;

namespace InfinityRush.Meta
{
    [CreateAssetMenu(fileName = "CharacterDefinition", menuName = "InfinityRush/Character Definition")]
    public class CharacterDefinition : ScriptableObject
    {
        public string characterId = "char_runner_teal";
        public string displayNameLocalizationKey = "CHAR_TEAL_RUNNER";
        public Sprite portrait;
        public GameObject characterPrefab;
        public int unlockCostCoins = 0;
        public bool isUnlockedByDefault = false;
    }
}
