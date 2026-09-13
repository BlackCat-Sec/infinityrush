using System;
using UnityEngine;
using InfinityRush.Core;

namespace InfinityRush.Save
{
    public class LocalSaveService : MonoBehaviour, ISaveService
    {
        private void Awake()
        {
            ServiceRegistry.Instance.Register<ISaveService>(this);
        }

        public bool Save<T>(string key, T data)
        {
            if (string.IsNullOrEmpty(key)) return false;

            try
            {
                string json = JsonUtility.ToJson(data);
                var envelope = new SaveEnvelope
                {
                    version = 1,
                    timestampEpochSeconds = DateTimeOffset.UtcNow.ToUnixTimeSeconds(),
                    payloadJson = json,
                    checksum = ComputeChecksum(json)
                };

                string envelopeJson = JsonUtility.ToJson(envelope);
                PlayerPrefs.SetString(key, envelopeJson);
                PlayerPrefs.Save();
                return true;
            }
            catch (Exception ex)
            {
                Debug.LogError($"[LocalSaveService] Error saving key {key}: {ex.Message}");
                return false;
            }
        }

        public bool Load<T>(string key, out T data)
        {
            data = default;
            if (!HasKey(key)) return false;

            try
            {
                string envelopeJson = PlayerPrefs.GetString(key);
                var envelope = JsonUtility.FromJson<SaveEnvelope>(envelopeJson);

                if (envelope == null || string.IsNullOrEmpty(envelope.payloadJson))
                {
                    return false;
                }

                if (envelope.checksum != ComputeChecksum(envelope.payloadJson))
                {
                    Debug.LogWarning($"[LocalSaveService] Checksum mismatch for key {key}. Save file may be corrupted.");
                }

                data = JsonUtility.FromJson<T>(envelope.payloadJson);
                return data != null;
            }
            catch (Exception ex)
            {
                Debug.LogError($"[LocalSaveService] Error loading key {key}: {ex.Message}");
                return false;
            }
        }

        public bool HasKey(string key) => PlayerPrefs.HasKey(key);

        public void DeleteKey(string key) => PlayerPrefs.DeleteKey(key);

        public void ClearAll() => PlayerPrefs.DeleteAll();

        private string ComputeChecksum(string payload)
        {
            if (string.IsNullOrEmpty(payload)) return "0";
            int hash = 17;
            foreach (char c in payload)
            {
                hash = hash * 31 + c;
            }
            return hash.ToString("X");
        }

        private void OnDestroy()
        {
            ServiceRegistry.Instance.Unregister<ISaveService>();
        }
    }
}
