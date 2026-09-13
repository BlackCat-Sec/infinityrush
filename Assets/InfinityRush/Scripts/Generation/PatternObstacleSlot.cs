using System;
using UnityEngine;

namespace InfinityRush.Generation
{
    public enum ObstacleSlotType
    {
        None,
        Hurdle,          // Jumpable low hurdle
        Barrier,         // Rollable high overhead signal
        SubwayTrain,     // Blocking train
        TrainRamp        // Sloping ramp onto train roof
    }

    [Serializable]
    public struct PatternObstacleSlot
    {
        public int lane;             // 0 = Left, 1 = Center, 2 = Right
        public float relativeZ;      // Offset along track segment (0f to segmentLength)
        public ObstacleSlotType obstacleType;
    }
}
