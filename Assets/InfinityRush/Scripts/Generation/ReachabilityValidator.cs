using System.Collections.Generic;

namespace InfinityRush.Generation
{
    public class ReachabilityValidator
    {
        public bool IsPatternReachable(TrackPatternDefinition pattern, int startingLane)
        {
            if (pattern == null || pattern.slots == null || pattern.slots.Count == 0)
                return true;

            // Group slots by Z slice
            var zSlices = new Dictionary<int, List<PatternObstacleSlot>>();
            foreach (var slot in pattern.slots)
            {
                int zKey = (int)(slot.relativeZ / 2.0f); // 2-unit slice tolerance
                if (!zSlices.TryGetValue(zKey, out var slice))
                {
                    slice = new List<PatternObstacleSlot>();
                    zSlices[zKey] = slice;
                }
                slice.Add(slot);
            }

            var currentLanes = new HashSet<int> { startingLane };

            foreach (var slice in zSlices.Values)
            {
                var nextLanes = new HashSet<int>();

                foreach (int lane in currentLanes)
                {
                    // Player can stay in current lane or shift to adjacent lane (if reachable)
                    for (int candidateLane = lane - 1; candidateLane <= lane + 1; candidateLane++)
                    {
                        if (candidateLane < 0 || candidateLane >= 3)
                            continue;

                        // Check if candidate lane in this slice is blocked completely
                        bool isBlocked = false;
                        foreach (var slot in slice)
                        {
                            if (slot.lane == candidateLane && slot.obstacleType == ObstacleSlotType.SubwayTrain)
                            {
                                isBlocked = true;
                                break;
                            }
                        }

                        if (!isBlocked)
                        {
                            nextLanes.Add(candidateLane);
                        }
                    }
                }

                if (nextLanes.Count == 0)
                {
                    return false; // Path impassable!
                }

                currentLanes = nextLanes;
            }

            return true;
        }
    }
}
