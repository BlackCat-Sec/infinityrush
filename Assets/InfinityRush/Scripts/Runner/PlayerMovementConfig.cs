using UnityEngine;

namespace InfinityRush.Runner
{
    [CreateAssetMenu(fileName = "PlayerMovementConfig", menuName = "InfinityRush/Player Movement Config")]
    public class PlayerMovementConfig : ScriptableObject
    {
        [Header("Lane Navigation")]
        public int laneCount = 3;
        public float laneSpacing = 2.5f;
        public float laneChangeSpeed = 18.0f;

        [Header("Vertical Locomotion")]
        public float jumpVelocity = 12.5f;
        public float gravity = 36.0f;
        public float fastDropGravityMultiplier = 2.2f;
        public float terminalVelocity = 50.0f;
        public float coyoteTime = 0.14f;
        public float jumpBufferTime = 0.16f;

        [Header("Sliding")]
        public float slideDuration = 0.65f;
        public float slideBufferTime = 0.18f;

        [Header("Collider Dimensions")]
        public float standingColliderHeight = 2.0f;
        public float standingColliderCenterY = 1.0f;
        public float slidingColliderHeight = 0.9f;
        public float slidingColliderCenterY = 0.45f;
    }
}
