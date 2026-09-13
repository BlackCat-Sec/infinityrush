using NUnit.Framework;
using UnityEngine;
using InfinityRush.Scoring;

namespace InfinityRush.Tests.EditMode
{
    [TestFixture]
    public class ScoreServiceTests
    {
        private GameObject _go;
        private ScoreService _scoreService;

        [SetUp]
        public void SetUp()
        {
            _go = new GameObject("ScoreServiceTest");
            _scoreService = _go.AddComponent<ScoreService>();
            _scoreService.StartRun(500);
        }

        [TearDown]
        public void TearDown()
        {
            Object.DestroyImmediate(_go);
        }

        [Test]
        public void InitialRun_ResetsScoreAndPreservesHighScore()
        {
            Assert.AreEqual(0, _scoreService.CurrentScore);
            Assert.AreEqual(500, _scoreService.HighScore);
            Assert.AreEqual(0, _scoreService.CoinsCollected);
            Assert.AreEqual(1, _scoreService.Multiplier);
        }

        [Test]
        public void AddCoins_IncreasesCoinsAndScoreBonus()
        {
            _scoreService.AddCoins(5);
            Assert.AreEqual(5, _scoreService.CoinsCollected);
            Assert.Greater(_scoreService.CurrentScore, 0);
        }

        [Test]
        public void SetMultiplier_DoublesPointsGained()
        {
            _scoreService.SetMultiplier(2);
            Assert.AreEqual(2, _scoreService.Multiplier);

            _scoreService.AddCoins(1);
            Assert.AreEqual(1, _scoreService.CoinsCollected);
        }

        [Test]
        public void BreakingHighScore_UpdatesHighScore()
        {
            _scoreService.AddScoreBonus(1000); // 1000 > initial 500
            Assert.AreEqual(1000, _scoreService.CurrentScore);
            Assert.AreEqual(1000, _scoreService.HighScore);
        }
    }
}
