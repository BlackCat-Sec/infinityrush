using UnityEngine;

namespace InfinityRush.Meta
{
    [CreateAssetMenu(fileName = "BoardDefinition", menuName = "InfinityRush/Board Definition")]
    public class BoardDefinition : ScriptableObject
    {
        public string boardId = "board_skyglide_neon";
        public string displayNameLocalizationKey = "BOARD_SKYGLIDE";
        public Sprite icon;
        public GameObject boardPrefab;
        public Color trailColor = Color.cyan;
        public int unlockCostCoins = 1500;
        public bool isUnlockedByDefault = false;
    }
}
