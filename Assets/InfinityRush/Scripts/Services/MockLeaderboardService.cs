using System;
using System.Collections.Generic;
using UnityEngine;
using InfinityRush.Core;

namespace InfinityRush.Services
{
    public class MockLeaderboardService : MonoBehaviour, ILeaderboardService
    {
        private readonly List<LeaderboardEntry> _mockEntries = new List<LeaderboardEntry>();

        private void Awake()
        {
            ServiceRegistry.Instance.Register<ILeaderboardService>(this);
            InitMockData();
        }

        private void InitMockData()
        {
            _mockEntries.Add(new LeaderboardEntry { playerId = "p1", playerName = "CyberRunner", rank = 1, score = 12500 });
            _mockEntries.Add(new LeaderboardEntry { playerId = "p2", playerName = "NeonDash", rank = 2, score = 9800 });
            _mockEntries.Add(new LeaderboardEntry { playerId = "p3", playerName = "RetroSurfer", rank = 3, score = 7400 });
        }

        public void SubmitScore(string leaderboardId, int score, Action<bool> onComplete)
        {
            if (score <= 0)
            {
                onComplete?.Invoke(false);
                return;
            }

            _mockEntries.Add(new LeaderboardEntry
            {
                playerId = "local_player",
                playerName = "You",
                rank = 0,
                score = score
            });

            _mockEntries.Sort((a, b) => b.score.CompareTo(a.score));

            for (int i = 0; i < _mockEntries.Count; i++)
            {
                var entry = _mockEntries[i];
                entry.rank = i + 1;
                _mockEntries[i] = entry;
            }

            onComplete?.Invoke(true);
        }

        public void GetTopScores(string leaderboardId, int limit, Action<List<LeaderboardEntry>> onComplete)
        {
            int count = Mathf.Min(limit, _mockEntries.Count);
            onComplete?.Invoke(_mockEntries.GetRange(0, count));
        }

        private void OnDestroy()
        {
            ServiceRegistry.Instance.Unregister<ILeaderboardService>();
        }
    }
}
