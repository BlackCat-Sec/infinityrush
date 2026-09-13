using System.Collections.Generic;
using UnityEngine;
using InfinityRush.Core;

namespace InfinityRush.Generation
{
    public class EnvironmentDirector : MonoBehaviour
    {
        [SerializeField] private List<EnvironmentTheme> districtThemes = new List<EnvironmentTheme>();
        [SerializeField] private float districtSwitchDistance = 1500f; // Switch district every 1500 meters

        public EnvironmentTheme CurrentTheme { get; private set; }
        public int CurrentDistrictIndex { get; private set; }

        private void Awake()
        {
            ServiceRegistry.Instance.Register(this);
            if (districtThemes != null && districtThemes.Count > 0)
            {
                CurrentTheme = districtThemes[0];
            }
        }

        public void UpdateDistance(float distanceTravelled)
        {
            if (districtThemes == null || districtThemes.Count == 0) return;

            int newIndex = Mathf.FloorToInt(distanceTravelled / districtSwitchDistance) % districtThemes.Count;
            if (newIndex != CurrentDistrictIndex)
            {
                CurrentDistrictIndex = newIndex;
                CurrentTheme = districtThemes[CurrentDistrictIndex];
                ApplyTheme(CurrentTheme);
            }
        }

        private void ApplyTheme(EnvironmentTheme theme)
        {
            if (theme == null) return;
            RenderSettings.fogColor = theme.fogColor;
        }

        private void OnDestroy()
        {
            ServiceRegistry.Instance.Unregister<EnvironmentDirector>();
        }
    }
}
