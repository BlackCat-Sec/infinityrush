using UnityEngine;

namespace InfinityRush.Generation
{
    public class TrackSegment : MonoBehaviour
    {
        [SerializeField] private float length = 30.0f;
        public float Length => length;

        public Transform EntryPoint;
        public Transform ExitPoint;

        public void SetLength(float len)
        {
            length = len;
        }
    }
}
