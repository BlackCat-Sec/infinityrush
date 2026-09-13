using UnityEngine;
using InfinityRush.Core;
using InfinityRush.Meta;

namespace InfinityRush.Save
{
    public class SaveCoordinator : MonoBehaviour
    {
        private const string ProfileSaveKey = "infinity_rush_profile";

        private ISaveService _saveService;
        public PlayerProfile Profile { get; private set; }

        private void Awake()
        {
            ServiceRegistry.Instance.Register(this);
        }

        private void Start()
        {
            if (ServiceRegistry.Instance.TryGet<ISaveService>(out _saveService))
            {
                LoadProfile();
            }
        }

        public void LoadProfile()
        {
            if (_saveService != null && _saveService.Load<PlayerProfile>(ProfileSaveKey, out var loaded))
            {
                Profile = loaded;
            }
            else
            {
                Profile = new PlayerProfile();
                Profile.InitDefaults();
                SaveProfile();
            }
        }

        public void SaveProfile()
        {
            if (_saveService != null && Profile != null)
            {
                _saveService.Save(ProfileSaveKey, Profile);
            }
        }

        private void OnDestroy()
        {
            SaveProfile();
            ServiceRegistry.Instance.Unregister<SaveCoordinator>();
        }
    }
}
