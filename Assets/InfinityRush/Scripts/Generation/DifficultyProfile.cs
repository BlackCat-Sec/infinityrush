using UnityEngine;

namespace InfinityRush.Generation
{
    [CreateAssetMenu(fileName = "DifficultyProfile", menuName = "InfinityRush/Difficulty Profile")]
    public class DifficultyProfile : ScriptableObject
    {
        public float initialWorldSpeed = 12.0f;
        public float maxWorldSpeed = 28.0f;
        public float speedRampRate = 0.2f; // speed increase per second

        public float initialSpawnDelay = 1.6f;
        public float minSpawnDelay = 0.7f;
        public float spawnDelayDecreaseRate = 0.015f;

        public int CalculateTier(float elapsedRunTimeSeconds)
        {
            return Mathf.FloorToInt(elapsedRunTimeSeconds / 10.0f);
        }

        public float CalculateWorldSpeed(float elapsedRunTimeSeconds)
        {
            return Mathf.Min(initialWorldSpeed + elapsedRunTimeSeconds * speedRampRate, maxWorldSpeed);
        }

        public float CalculateSpawnDelay(float elapsedRunTimeSeconds)
        {
            return Mathf.Max(initialSpawnDelay - elapsedRunTimeSeconds * spawnDelayDecreaseRate, minSpawnDelay);
        }
    }
}
