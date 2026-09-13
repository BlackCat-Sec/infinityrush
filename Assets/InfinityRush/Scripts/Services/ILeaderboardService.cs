using System;
using System.Collections.Generic;

namespace InfinityRush.Services
{
    public struct LeaderboardEntry
    {
        public string playerId;
        public string playerName;
        public int rank;
        public int score;
    }

    public interface ILeaderboardService
    {
        void SubmitScore(string leaderboardId, int score, Action<bool> onComplete);
        void GetTopScores(string leaderboardId, int limit, Action<List<LeaderboardEntry>> onComplete);
    }
}
