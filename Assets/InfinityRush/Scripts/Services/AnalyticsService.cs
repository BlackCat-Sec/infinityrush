using System.Collections.Generic;
using UnityEngine;
using InfinityRush.Core;

namespace InfinityRush.Services
{
    public class AnalyticsService : MonoBehaviour, IAnalyticsService
    {
        private void Awake()
        {
            ServiceRegistry.Instance.Register<IAnalyticsService>(this);
        }

        public void LogEvent(string eventName, Dictionary<string, object> parameters = null)
        {
            Debug.Log($"[Analytics] Event: {eventName}");
        }

        public void LogRunStarted(string runId, string characterId, string boardId)
        {
            Debug.Log($"[Analytics] Run Started: {runId}, Char: {characterId}, Board: {boardId}");
        }

        public void LogRunEnded(int score, float distance, int coins, string deathReason)
        {
            Debug.Log($"[Analytics] Run Ended: Score={score}, Distance={distance}m, Coins={coins}, Reason={deathReason}");
        }

        private void OnDestroy()
        {
            ServiceRegistry.Instance.Unregister<IAnalyticsService>();
        }
    }
}
