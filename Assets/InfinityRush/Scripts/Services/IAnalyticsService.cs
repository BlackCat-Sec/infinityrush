using System.Collections.Generic;

namespace InfinityRush.Services
{
    public interface IAnalyticsService
    {
        void LogEvent(string eventName, Dictionary<string, object> parameters = null);
        void LogRunStarted(string runId, string characterId, string boardId);
        void LogRunEnded(int score, float distance, int coins, string deathReason);
    }
}
