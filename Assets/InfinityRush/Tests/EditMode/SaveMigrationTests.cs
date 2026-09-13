using NUnit.Framework;
using UnityEngine;
using InfinityRush.Save;
using InfinityRush.Meta;

namespace InfinityRush.Tests.EditMode
{
    [TestFixture]
    public class SaveMigrationTests
    {
        private GameObject _go;
        private LocalSaveService _saveService;

        [SetUp]
        public void SetUp()
        {
            _go = new GameObject("SaveMigrationTest");
            _saveService = _go.AddComponent<LocalSaveService>();
            _saveService.ClearAll();
        }

        [TearDown]
        public void TearDown()
        {
            _saveService.ClearAll();
            Object.DestroyImmediate(_go);
        }

        [Test]
        public void SaveAndLoad_PreservesProfileData()
        {
            var profile = new PlayerProfile();
            profile.InitDefaults();
            profile.highScore = 1500;
            profile.totalCoins = 250;

            bool saveResult = _saveService.Save("test_profile", profile);
            Assert.IsTrue(saveResult);

            bool loadResult = _saveService.Load<PlayerProfile>("test_profile", out var loaded);
            Assert.IsTrue(loadResult);
            Assert.IsNotNull(loaded);
            Assert.AreEqual(1500, loaded.highScore);
            Assert.AreEqual(250, loaded.totalCoins);
        }

        [Test]
        public void LoadNonExistentKey_ReturnsFalse()
        {
            bool loadResult = _saveService.Load<PlayerProfile>("non_existent_key", out var loaded);
            Assert.IsFalse(loadResult);
            Assert.IsNull(loaded);
        }
    }
}
