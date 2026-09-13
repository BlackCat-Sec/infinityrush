using UnityEngine;
using InfinityRush.Core;

namespace InfinityRush.Audio
{
    public class HapticManager : MonoBehaviour
    {
        public bool isHapticsEnabled = true;

        private void Awake()
        {
            ServiceRegistry.Instance.Register(this);
        }

        public void TriggerLightFeedback()
        {
            if (!isHapticsEnabled) return;
#if UNITY_ANDROID && !UNITY_EDITOR
            try {
                using (var unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
                using (var currentActivity = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity"))
                using (var vibrator = currentActivity.Call<AndroidJavaObject>("getSystemService", "vibrator")) {
                    vibrator?.Call("vibrate", 25L);
                }
            } catch {}
#endif
        }

        public void TriggerHeavyFeedback()
        {
            if (!isHapticsEnabled) return;
#if UNITY_ANDROID && !UNITY_EDITOR
            try {
                using (var unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
                using (var currentActivity = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity"))
                using (var vibrator = currentActivity.Call<AndroidJavaObject>("getSystemService", "vibrator")) {
                    vibrator?.Call("vibrate", 120L);
                }
            } catch {}
#endif
        }

        private void OnDestroy()
        {
            ServiceRegistry.Instance.Unregister<HapticManager>();
        }
    }
}
