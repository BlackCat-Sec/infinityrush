using UnityEngine;

namespace InfinityRush.Generation
{
    public enum DistrictType
    {
        NexusCentral,   // District 1: Bright futuristic eco-city with high-rises, waterfalls
        NeonNight,      // District 2: Rainy night downtown with neon cyan/magenta reflections
        MetroTunnel,    // District 3: Underground enclosed metro tunnel with emergency lights
        SkyHarbor,      // District 4: Open coastal bridges, ocean, elevated rails
        ConstructionZone// District 5: Cranes, maintenance platforms, suspended cargo
    }

    [CreateAssetMenu(fileName = "EnvironmentTheme", menuName = "InfinityRush/Environment Theme")]
    public class EnvironmentTheme : ScriptableObject
    {
        public DistrictType districtType;
        public string districtName = "NEXUS CENTRAL";
        public Color skyTopColor = Color.parseColor("#0F172A");
        public Color skyBottomColor = Color.parseColor("#581C87");
        public Color fogColor = Color.parseColor("#311B92");
        public Color railGlowColor = Color.parseColor("#06B6D4");
        public Color trainColor = Color.parseColor("#0284C7");

        public string[] signPhrases = new string[]
        {
            "NEXUS METRO",
            "FURTHER TOGETHER",
            "RUN FURTHER",
            "BUILD A BRIGHTER TOMORROW",
            "METRO CORE"
        };
    }
}
