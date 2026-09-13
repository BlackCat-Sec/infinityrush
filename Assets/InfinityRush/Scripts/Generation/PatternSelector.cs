using System.Collections.Generic;
using UnityEngine;

namespace InfinityRush.Generation
{
    public class PatternSelector
    {
        private readonly List<TrackPatternDefinition> _patterns;
        private readonly ReachabilityValidator _validator;

        public PatternSelector(List<TrackPatternDefinition> patterns)
        {
            _patterns = patterns ?? new List<TrackPatternDefinition>();
            _validator = new ReachabilityValidator();
        }

        public TrackPatternDefinition SelectPattern(int difficultyTier, int currentLane, RunRandom random)
        {
            var validPatterns = new List<TrackPatternDefinition>();

            foreach (var p in _patterns)
            {
                if (difficultyTier >= p.minDifficultyTier && difficultyTier <= p.maxDifficultyTier)
                {
                    if (_validator.IsPatternReachable(p, currentLane))
                    {
                        validPatterns.Add(p);
                    }
                }
            }

            if (validPatterns.Count == 0)
            {
                return _patterns.Count > 0 ? _patterns[0] : null;
            }

            int index = random.NextInt(0, validPatterns.Count);
            return validPatterns[index];
        }
    }
}
