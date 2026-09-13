using System;

namespace InfinityRush.Save
{
    [Serializable]
    public class SaveEnvelope
    {
        public int version = 1;
        public long timestampEpochSeconds;
        public string payloadJson;
        public string checksum;
    }
}
