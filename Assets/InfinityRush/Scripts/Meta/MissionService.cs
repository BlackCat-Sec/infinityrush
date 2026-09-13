using System;
using System.Collections.Generic;
using UnityEngine;
using InfinityRush.Core;

namespace InfinityRush.Meta
{
    public interface IMissionService
    {
        event Action<MissionDefinition> OnMissionCompleted;

        void TrackProgress(MissionType type, int amount);
        bool IsMissionCompleted(string missionId);
    }

    public class MissionService : MonoBehaviour, IMissionService
    {
        public event Action<MissionDefinition> OnMissionCompleted;

        [SerializeField] private List<MissionDefinition> activeMissions = new List<MissionDefinition>();
        private readonly Dictionary<string, MissionProgress> _progressMap = new Dictionary<string, MissionProgress>();

        private void Awake()
        {
            ServiceRegistry.Instance.Register<IMissionService>(this);
            InitProgressMap();
        }

        private void InitProgressMap()
        {
            foreach (var mission in activeMissions)
            {
                if (mission != null && !_progressMap.ContainsKey(mission.missionId))
                {
                    _progressMap[mission.missionId] = new MissionProgress(mission.missionId);
                }
            }
        }

        public void TrackProgress(MissionType type, int amount)
        {
            if (amount <= 0) return;

            foreach (var mission in activeMissions)
            {
                if (mission != null && mission.missionType == type)
                {
                    if (_progressMap.TryGetValue(mission.missionId, out var progress) && !progress.isCompleted)
                    {
                        progress.currentAmount += amount;
                        if (progress.currentAmount >= mission.targetAmount)
                        {
                            progress.isCompleted = true;
                            OnMissionCompleted?.Invoke(mission);
                        }
                    }
                }
            }
        }

        public bool IsMissionCompleted(string missionId)
        {
            return _progressMap.TryGetValue(missionId, out var progress) && progress.isCompleted;
        }

        private void OnDestroy()
        {
            ServiceRegistry.Instance.Unregister<IMissionService>();
        }
    }
}
