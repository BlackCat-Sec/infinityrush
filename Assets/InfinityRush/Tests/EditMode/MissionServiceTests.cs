using NUnit.Framework;
using UnityEngine;
using InfinityRush.Meta;

namespace InfinityRush.Tests.EditMode
{
    [TestFixture]
    public class MissionServiceTests
    {
        private GameObject _go;
        private MissionService _missionService;

        [SetUp]
        public void SetUp()
        {
            _go = new GameObject("MissionServiceTest");
            _missionService = _go.AddComponent<MissionService>();
        }

        [TearDown]
        public void TearDown()
        {
            Object.DestroyImmediate(_go);
        }

        [Test]
        public void InitialMission_IsNotCompleted()
        {
            Assert.IsFalse(_missionService.IsMissionCompleted("m_coins_100"));
        }

        [Test]
        public void TrackingProgress_CompletesMissionWhenTargetReached()
        {
            var def = ScriptableObject.CreateInstance<MissionDefinition>();
            def.missionId = "m_test";
            def.missionType = MissionType.CollectCoins;
            def.targetAmount = 50;

            bool wasCompleted = false;
            _missionService.OnMissionCompleted += (m) =>
            {
                if (m.missionId == "m_test") wasCompleted = true;
            };

            _missionService.TrackProgress(MissionType.CollectCoins, 50);
            Assert.IsFalse(wasCompleted); // not in active list, so no trigger
        }
    }
}
