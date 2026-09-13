using UnityEngine;

namespace InfinityRush.Collectibles
{
    public abstract class Collectible : MonoBehaviour
    {
        public int lane;
        public float relativeZ;
        public float yOffset;

        public abstract void OnCollected(GameObject collector);
    }
}
