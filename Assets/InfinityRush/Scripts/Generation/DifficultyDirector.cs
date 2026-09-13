using UnityEngine;

namespace InfinityRush.Generation
{
    public class DifficultyDirector : MonoBehaviour
    {
        [SerializeField] private DifficultyProfile profile;

        public float ElapsedRunTime { get; private set; }
        public float CurrentWorldSpeed { get; private set; }
        public float CurrentSpawnDelay { get; private set; }
        public int CurrentTier { get; private set; }

        private void Awake()
        {
            if (profile == null)
            {
                profile = ScriptableObject.CreateInstance<DifficultyProfile>();
            }
            ResetDifficulty();
        }

        public void SetProfile(DifficultyProfile newProfile)
        {
            profile = newProfile;
        }

        public void ResetDifficulty()
        {
            ElapsedRunTime = 0f;
            CurrentWorldSpeed = profile.initialWorldSpeed;
            CurrentSpawnDelay = profile.initialSpawnDelay;
            CurrentTier = 0;
        }

        public void UpdateDifficulty(float deltaTime)
        {
            ElapsedRunTime += deltaTime;
            CurrentWorldSpeed = profile.CalculateWorldSpeed(ElapsedRunTime);
            CurrentSpawnDelay = profile.CalculateSpawnDelay(ElapsedRunTime);
            CurrentTier = profile.CalculateTier(ElapsedRunTime);
        }
    }
}
