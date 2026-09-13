using System.Collections.Generic;
using NUnit.Framework;
using UnityEngine;
using InfinityRush.Generation;

namespace InfinityRush.Tests.EditMode
{
    [TestFixture]
    public class ReachabilityValidatorTests
    {
        private ReachabilityValidator _validator;

        [SetUp]
        public void SetUp()
        {
            _validator = new ReachabilityValidator();
        }

        [Test]
        public void EmptyPattern_IsAlwaysReachable()
        {
            var pattern = ScriptableObject.CreateInstance<TrackPatternDefinition>();
            pattern.slots = new List<PatternObstacleSlot>();

            bool result = _validator.IsPatternReachable(pattern, 1);
            Assert.IsTrue(result);
        }

        [Test]
        public void SingleBlockInOneLane_IsReachableFromOtherLane()
        {
            var pattern = ScriptableObject.CreateInstance<TrackPatternDefinition>();
            pattern.slots = new List<PatternObstacleSlot>
            {
                new PatternObstacleSlot { lane = 0, relativeZ = 10f, obstacleType = ObstacleSlotType.SubwayTrain }
            };

            bool result = _validator.IsPatternReachable(pattern, 1); // Starting in lane 1 (center)
            Assert.IsTrue(result);
        }

        [Test]
        public void FullWallInAll3Lanes_IsUnreachable()
        {
            var pattern = ScriptableObject.CreateInstance<TrackPatternDefinition>();
            pattern.slots = new List<PatternObstacleSlot>
            {
                new PatternObstacleSlot { lane = 0, relativeZ = 10f, obstacleType = ObstacleSlotType.SubwayTrain },
                new PatternObstacleSlot { lane = 1, relativeZ = 10f, obstacleType = ObstacleSlotType.SubwayTrain },
                new PatternObstacleSlot { lane = 2, relativeZ = 10f, obstacleType = ObstacleSlotType.SubwayTrain }
            };

            bool result = _validator.IsPatternReachable(pattern, 1);
            Assert.IsFalse(result);
        }
    }
}
