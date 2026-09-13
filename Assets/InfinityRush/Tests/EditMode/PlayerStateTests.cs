using NUnit.Framework;
using InfinityRush.Runner;

namespace InfinityRush.Tests.EditMode
{
    [TestFixture]
    public class PlayerStateTests
    {
        private PlayerLocomotionStateMachine _stateMachine;

        [SetUp]
        public void SetUp()
        {
            _stateMachine = new PlayerLocomotionStateMachine();
        }

        [Test]
        public void InitialState_DefaultsToGrounded()
        {
            var dummyState = new DummyState(PlayerLocomotionState.Grounded);
            _stateMachine.RegisterState(dummyState);
            _stateMachine.ChangeState(PlayerLocomotionState.Grounded);

            Assert.AreEqual(PlayerLocomotionState.Grounded, _stateMachine.CurrentStateType);
        }

        [Test]
        public void StateChange_TriggersStateTransition()
        {
            var groundedState = new DummyState(PlayerLocomotionState.Grounded);
            var jumpingState = new DummyState(PlayerLocomotionState.Jumping);

            _stateMachine.RegisterState(groundedState);
            _stateMachine.RegisterState(jumpingState);

            _stateMachine.ChangeState(PlayerLocomotionState.Grounded);
            Assert.IsTrue(groundedState.IsEntered);

            _stateMachine.ChangeState(PlayerLocomotionState.Jumping);
            Assert.IsTrue(groundedState.IsExited);
            Assert.IsTrue(jumpingState.IsEntered);
            Assert.AreEqual(PlayerLocomotionState.Jumping, _stateMachine.CurrentStateType);
        }

        private class DummyState : IPlayerLocomotionState
        {
            public PlayerLocomotionState StateType { get; }
            public bool IsEntered { get; private set; }
            public bool IsExited { get; private set; }

            public DummyState(PlayerLocomotionState type)
            {
                StateType = type;
            }

            public void Enter() => IsEntered = true;
            public void Exit() => IsExited = true;
            public void Update(float deltaTime) { }
        }
    }
}
