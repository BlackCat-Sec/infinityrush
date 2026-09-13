using NUnit.Framework;
using UnityEngine;
using InfinityRush.PowerUps;

namespace InfinityRush.Tests.EditMode
{
    [TestFixture]
    public class PowerUpTests
    {
        private GameObject _go;
        private PowerUpController _controller;

        [SetUp]
        public void SetUp()
        {
            _go = new GameObject("PowerUpTest");
            _controller = _go.AddComponent<PowerUpController>();
        }

        [TearDown]
        public void TearDown()
        {
            Object.DestroyImmediate(_go);
        }

        [Test]
        public void ApplyingMagnet_ActivatesMagnetEffect()
        {
            var def = ScriptableObject.CreateInstance<PowerUpDefinition>();
            def.powerUpType = PowerUpType.CoinMagnet;
            def.duration = 5.0f;

            _controller.ApplyPowerUp(def);

            Assert.IsTrue(_controller.IsEffectActive(PowerUpType.CoinMagnet));
            Assert.AreEqual(5.0f, _controller.GetRemainingTime(PowerUpType.CoinMagnet));
        }

        [Test]
        public void CancelAll_ExpiresAllActivePowerUps()
        {
            var def = ScriptableObject.CreateInstance<PowerUpDefinition>();
            def.powerUpType = PowerUpType.CoinMagnet;
            def.duration = 5.0f;

            _controller.ApplyPowerUp(def);
            Assert.IsTrue(_controller.IsEffectActive(PowerUpType.CoinMagnet));

            _controller.CancelAll();
            Assert.IsFalse(_controller.IsEffectActive(PowerUpType.CoinMagnet));
        }
    }
}
