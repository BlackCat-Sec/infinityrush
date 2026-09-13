using System;
using UnityEngine;

namespace InfinityRush.Runner
{
    public class AnimationBridge : MonoBehaviour
    {
        [SerializeField] private Animator animator;

        private static readonly int IsGroundedHash = Animator.StringToHash("IsGrounded");
        private static readonly int IsSlidingHash = Animator.StringToHash("IsSliding");
        private static readonly int JumpTriggerHash = Animator.StringToHash("JumpTrigger");
        private static readonly int LaneChangeTriggerHash = Animator.StringToHash("LaneChangeTrigger");

        public event Action OnFootstepEvent;
        public event Action OnJumpEvent;
        public event Action OnSlideEvent;

        private void Awake()
        {
            if (animator == null)
            {
                animator = GetComponent<Animator>() ?? GetComponentInChildren<Animator>();
            }
        }

        public void SetGrounded(bool isGrounded)
        {
            if (animator != null && animator.runtimeAnimatorController != null)
            {
                animator.SetBool(IsGroundedHash, isGrounded);
            }
        }

        public void SetSliding(bool isSliding)
        {
            if (animator != null && animator.runtimeAnimatorController != null)
            {
                animator.SetBool(IsSlidingHash, isSliding);
            }
        }

        public void TriggerJump()
        {
            if (animator != null && animator.runtimeAnimatorController != null)
            {
                animator.SetTrigger(JumpTriggerHash);
            }
            OnJumpEvent?.Invoke();
        }

        public void TriggerLaneChange()
        {
            if (animator != null && animator.runtimeAnimatorController != null)
            {
                animator.SetTrigger(LaneChangeTriggerHash);
            }
        }

        // Animation Event Callbacks
        public void OnFootstep() => OnFootstepEvent?.Invoke();
        public void OnJump() => OnJumpEvent?.Invoke();
        public void OnSlide() => OnSlideEvent?.Invoke();
    }
}
