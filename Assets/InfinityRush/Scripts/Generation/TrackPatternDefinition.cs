using System.Collections.Generic;
using UnityEngine;

namespace InfinityRush.Generation
{
    [CreateAssetMenu(fileName = "TrackPatternDefinition", menuName = "InfinityRush/Track Pattern Definition")]
    public class TrackPatternDefinition : ScriptableObject
    {
        public string patternId;
        public float segmentLength = 30.0f;
        public int minDifficultyTier = 0;
        public int maxDifficultyTier = 10;

        public List<PatternObstacleSlot> slots = new List<PatternObstacleSlot>();
    }
}
