using UnityEngine;

namespace InfinityRush.Meta
{
    public enum MissionType
    {
        CollectCoins,
        ScorePoints,
        SurviveDuration,
        PerformJumps,
        PerformSlides
    }

    [CreateAssetMenu(fileName = "MissionDefinition", menuName = "InfinityRush/Mission Definition")]
    public class MissionDefinition : ScriptableObject
    {
        public string missionId;
        public MissionType missionType;
        public string descriptionLocalizationKey;
        public int targetAmount = 100;
        public int rewardCoins = 250;
    }
}
