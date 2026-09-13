using UnityEngine;
using InfinityRush.Input;

namespace InfinityRush.Runner
{
    public class RunnerMotor : MonoBehaviour
    {
        [SerializeField] private PlayerMovementConfig config;
        [SerializeField] private CharacterController characterController;

        public PlayerMovementConfig Config => config;

        public int CurrentLane { get; private set; } = 1; // 0 = Left, 1 = Center, 2 = Right
        public float TargetX => (CurrentLane - 1) * config.laneSpacing;
        public float CurrentX { get; private set; }

        public float VerticalVelocity { get; private set; }
        public float CoyoteTimer { get; private set; }
        public float JumpBufferTimer { get; private set; }
        public float SlideBufferTimer { get; private set; }
        public float SlideDurationTimer { get; private set; }

        public bool IsGrounded { get; private set; }
        public bool IsSliding => SlideDurationTimer > 0f;
        public bool IsFastDropping { get; private set; }

        private void Awake()
        {
            if (characterController == null)
            {
                characterController = GetComponent<CharacterController>();
            }
            if (config == null)
            {
                config = ScriptableObject.CreateInstance<PlayerMovementConfig>();
            }
            ResetMotor();
        }

        public void SetConfig(PlayerMovementConfig newConfig)
        {
            config = newConfig;
        }

        public void ResetMotor()
        {
            CurrentLane = 1;
            CurrentX = 0f;
            VerticalVelocity = 0f;
            CoyoteTimer = config.coyoteTime;
            JumpBufferTimer = 0f;
            SlideBufferTimer = 0f;
            SlideDurationTimer = 0f;
            IsGrounded = true;
            IsFastDropping = false;

            ApplyStandingCollider();
        }

        public bool MoveLeft()
        {
            if (CurrentLane > 0)
            {
                CurrentLane--;
                return true;
            }
            return false;
        }

        public bool MoveRight()
        {
            if (CurrentLane < config.laneCount - 1)
            {
                CurrentLane++;
                return true;
            }
            return false;
        }

        public void QueueJump()
        {
            JumpBufferTimer = config.jumpBufferTime;
        }

        public void QueueSlide() {
            SlideBufferTimer = config.slideBufferTime;
            if (!IsGrounded && !IsFastDropping)
            {
                IsFastDropping = true;
            }
        }

        public void ExecuteJump()
        {
            VerticalVelocity = config.jumpVelocity;
            CoyoteTimer = 0f;
            JumpBufferTimer = 0f;
            IsGrounded = false;
            IsFastDropping = false;
            CancelSlide();
        }

        public void ExecuteSlide()
        {
            SlideDurationTimer = config.slideDuration;
            SlideBufferTimer = 0f;
            ApplySlidingCollider();
        }

        public void CancelSlide()
        {
            SlideDurationTimer = 0f;
            ApplyStandingCollider();
        }

        public void UpdateMotor(float deltaTime)
        {
            JumpBufferTimer = Mathf.Max(0f, JumpBufferTimer - deltaTime);
            SlideBufferTimer = Mathf.Max(0f, SlideBufferTimer - deltaTime);

            if (SlideDurationTimer > 0f)
            {
                SlideDurationTimer -= deltaTime;
                if (SlideDurationTimer <= 0f)
                {
                    ApplyStandingCollider();
                }
            }

            CurrentX = Mathf.Lerp(CurrentX, TargetX, deltaTime * config.laneChangeSpeed);

            if (IsGrounded)
            {
                CoyoteTimer = config.coyoteTime;
            }
            else
            {
                CoyoteTimer = Mathf.Max(0f, CoyoteTimer - deltaTime);
            }

            float currentGravity = config.gravity * (IsFastDropping ? config.fastDropGravityMultiplier : 1.0f);
            VerticalVelocity = Mathf.MoveTowards(VerticalVelocity, -config.terminalVelocity, currentGravity * deltaTime);

            Vector3 move = new Vector3(CurrentX - transform.position.x, VerticalVelocity * deltaTime, 0f);

            if (characterController != null && characterController.enabled)
            {
                characterController.Move(move);
                IsGrounded = characterController.isGrounded;
            }
            else
            {
                transform.position += move;
                IsGrounded = transform.position.y <= 0.01f;
                if (IsGrounded)
                {
                    Vector3 p = transform.position;
                    p.y = 0f;
                    transform.position = p;
                }
            }

            if (IsGrounded && VerticalVelocity <= 0f)
            {
                VerticalVelocity = 0f;
                if (IsFastDropping)
                {
                    IsFastDropping = false;
                    ExecuteSlide();
                }
            }
        }

        public void ApplyStandingCollider()
        {
            if (characterController != null)
            {
                characterController.height = config.standingColliderHeight;
                characterController.center = new Vector3(0f, config.standingColliderCenterY, 0f);
            }
        }

        public void ApplySlidingCollider()
        {
            if (characterController != null)
            {
                characterController.height = config.slidingColliderHeight;
                characterController.center = new Vector3(0f, config.slidingColliderCenterY, 0f);
            }
        }
    }
}
