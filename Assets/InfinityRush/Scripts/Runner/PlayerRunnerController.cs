using UnityEngine;
using InfinityRush.Core;
using InfinityRush.Input;

namespace InfinityRush.Runner
{
    [RequireComponent(typeof(RunnerMotor))]
    public class PlayerRunnerController : MonoBehaviour
    {
        [SerializeField] private PlayerMovementConfig config;
        [SerializeField] private RunnerMotor motor;
        [SerializeField] private AnimationBridge animationBridge;

        public RunnerMotor Motor => motor;
        public PlayerMovementConfig Config => config;

        private IInputReader _inputReader;
        private PlayerLocomotionStateMachine _stateMachine;

        private void Awake()
        {
            if (motor == null)
            {
                motor = GetComponent<RunnerMotor>();
            }
            if (config != null)
            {
                motor.SetConfig(config);
            }

            _stateMachine = new PlayerLocomotionStateMachine();
            InitStateMachine();
        }

        private void Start()
        {
            if (ServiceRegistry.Instance.TryGet<IInputReader>(out var reader))
            {
                BindInputReader(reader);
            }
        }

        public void BindInputReader(IInputReader reader)
        {
            if (_inputReader != null)
            {
                _inputReader.OnCommand -= HandleCommand;
            }
            _inputReader = reader;
            if (_inputReader != null)
            {
                _inputReader.OnCommand += HandleCommand;
            }
        }

        private void InitStateMachine()
        {
            // Simple locomotion states
            _stateMachine.RegisterState(new GroundedLocomotionState(_stateMachine, this));
            _stateMachine.RegisterState(new JumpingLocomotionState(_stateMachine, this));
            _stateMachine.RegisterState(new FallingLocomotionState(_stateMachine, this));
            _stateMachine.RegisterState(new SlidingLocomotionState(_stateMachine, this));
            _stateMachine.RegisterState(new FastDroppingLocomotionState(_stateMachine, this));

            _stateMachine.ChangeState(PlayerLocomotionState.Grounded);
        }

        private void Update()
        {
            float dt = Time.deltaTime;
            motor.UpdateMotor(dt);
            _stateMachine.Update(dt);

            if (animationBridge != null)
            {
                animationBridge.SetGrounded(motor.IsGrounded);
                animationBridge.SetSliding(motor.IsSliding);
            }

            EvaluateTransitions();
        }

        private void EvaluateTransitions()
        {
            if (motor.IsGrounded)
            {
                if (motor.JumpBufferTimer > 0f)
                {
                    motor.ExecuteJump();
                    if (animationBridge != null) animationBridge.TriggerJump();
                    _stateMachine.ChangeState(PlayerLocomotionState.Jumping);
                }
                else if (motor.SlideBufferTimer > 0f)
                {
                    motor.ExecuteSlide();
                    _stateMachine.ChangeState(PlayerLocomotionState.Sliding);
                }
                else if (motor.IsSliding)
                {
                    _stateMachine.ChangeState(PlayerLocomotionState.Sliding);
                }
                else
                {
                    _stateMachine.ChangeState(PlayerLocomotionState.Grounded);
                }
            }
            else
            {
                if (motor.IsFastDropping)
                {
                    _stateMachine.ChangeState(PlayerLocomotionState.FastDropping);
                }
                else if (motor.VerticalVelocity > 0f)
                {
                    _stateMachine.ChangeState(PlayerLocomotionState.Jumping);
                }
                else
                {
                    _stateMachine.ChangeState(PlayerLocomotionState.Falling);
                }
            }
        }

        public void HandleCommand(RunnerCommand command)
        {
            switch (command)
            {
                case RunnerCommand.MoveLeft:
                    if (motor.MoveLeft() && animationBridge != null)
                    {
                        animationBridge.TriggerLaneChange();
                    }
                    break;
                case RunnerCommand.MoveRight:
                    if (motor.MoveRight() && animationBridge != null)
                    {
                        animationBridge.TriggerLaneChange();
                    }
                    break;
                case RunnerCommand.Jump:
                    if (motor.IsGrounded || motor.CoyoteTimer > 0f)
                    {
                        motor.ExecuteJump();
                        if (animationBridge != null) animationBridge.TriggerJump();
                        _stateMachine.ChangeState(PlayerLocomotionState.Jumping);
                    }
                    else
                    {
                        motor.QueueJump();
                    }
                    break;
                case RunnerCommand.Slide:
                    if (motor.IsGrounded)
                    {
                        motor.ExecuteSlide();
                        _stateMachine.ChangeState(PlayerLocomotionState.Sliding);
                    }
                    else
                    {
                        motor.QueueSlide();
                    }
                    break;
            }
        }

        private void OnDestroy()
        {
            if (_inputReader != null)
            {
                _inputReader.OnCommand -= HandleCommand;
            }
        }
    }

    public abstract class BasePlayerLocomotionState : IPlayerLocomotionState
    {
        public abstract PlayerLocomotionState StateType { get; }
        protected readonly PlayerLocomotionStateMachine StateMachine;
        protected readonly PlayerRunnerController Controller;

        protected BasePlayerLocomotionState(PlayerLocomotionStateMachine stateMachine, PlayerRunnerController controller)
        {
            StateMachine = stateMachine;
            Controller = controller;
        }

        public virtual void Enter() { }
        public virtual void Exit() { }
        public virtual void Update(float deltaTime) { }
    }

    public class GroundedLocomotionState : BasePlayerLocomotionState
    {
        public override PlayerLocomotionState StateType => PlayerLocomotionState.Grounded;
        public GroundedLocomotionState(PlayerLocomotionStateMachine sm, PlayerRunnerController c) : base(sm, c) { }
    }

    public class JumpingLocomotionState : BasePlayerLocomotionState
    {
        public override PlayerLocomotionState StateType => PlayerLocomotionState.Jumping;
        public JumpingLocomotionState(PlayerLocomotionStateMachine sm, PlayerRunnerController c) : base(sm, c) { }
    }

    public class FallingLocomotionState : BasePlayerLocomotionState
    {
        public override PlayerLocomotionState StateType => PlayerLocomotionState.Falling;
        public FallingLocomotionState(PlayerLocomotionStateMachine sm, PlayerRunnerController c) : base(sm, c) { }
    }

    public class SlidingLocomotionState : BasePlayerLocomotionState
    {
        public override PlayerLocomotionState StateType => PlayerLocomotionState.Sliding;
        public SlidingLocomotionState(PlayerLocomotionStateMachine sm, PlayerRunnerController c) : base(sm, c) { }
    }

    public class FastDroppingLocomotionState : BasePlayerLocomotionState
    {
        public override PlayerLocomotionState StateType => PlayerLocomotionState.FastDropping;
        public FastDroppingLocomotionState(PlayerLocomotionStateMachine sm, PlayerRunnerController c) : base(sm, c) { }
    }
}
