using System;

namespace InfinityRush.Meta
{
    [Serializable]
    public class MissionProgress
    {
        public string missionId;
        public int currentAmount;
        public bool isCompleted;

        public MissionProgress(string id)
        {
            missionId = id;
            currentAmount = 0;
            isCompleted = false;
        }
    }
}
