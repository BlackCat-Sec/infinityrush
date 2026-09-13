using System.Collections;
using NUnit.Framework;
using UnityEngine;
using UnityEngine.TestTools;
using InfinityRush.Runner;
using InfinityRush.Input;

namespace InfinityRush.Tests.PlayMode
{
    public class PlayerMovementTests
    {
        private GameObject _playerObject;
        private PlayerRunnerController _controller;
        private RunnerMotor _motor;

        [SetUp]
        public void SetUp()
        {
            _playerObject = new GameObject("PlayerTest");
            _motor = _playerObject.AddComponent<RunnerMotor>();
            _controller = _playerObject.AddComponent<PlayerRunnerController>();

            var config = ScriptableObject.CreateInstance<PlayerMovementConfig>();
            config.laneCount = 3;
            config.laneSpacing = 2.5f;
            config.laneChangeSpeed = 50.0f;
            config.jumpVelocity = 12.0f;
            config.gravity = 35.0f;

            _motor.SetConfig(config);
            _motor.ResetMotor();
        }

        [TearDown]
        public void TearDown()
        {
            Object.Destroy(_playerObject);
        }

        [Test]
        public void InitialLane_StartsInCenterLane()
        {
            Assert.AreEqual(1, _motor.CurrentLane);
            Assert.AreEqual(0f, _motor.TargetX);
        }

        [Test]
        public void MoveLeft_ChangesLaneToLeft()
        {
            _controller.HandleCommand(RunnerCommand.MoveLeft);
            Assert.AreEqual(0, _motor.CurrentLane);
            Assert.AreEqual(-2.5f, _motor.TargetX);
        }

        [Test]
        public void MoveLeft_AtLeftEdge_ClampsToLeftLane()
        {
            _controller.HandleCommand(RunnerCommand.MoveLeft); // Lane 0
            _controller.HandleCommand(RunnerCommand.MoveLeft); // Still Lane 0
            Assert.AreEqual(0, _motor.CurrentLane);
        }

        [Test]
        public void MoveRight_AtRightEdge_ClampsToRightLane()
        {
            _controller.HandleCommand(RunnerCommand.MoveRight); // Lane 2
            _controller.HandleCommand(RunnerCommand.MoveRight); // Still Lane 2
            Assert.AreEqual(2, _motor.CurrentLane);
        }

        [UnityTest]
        public IEnumerator Jump_ExecutesJump_AndReturnsToGround()
        {
            _controller.HandleCommand(RunnerCommand.Jump);
            Assert.Greater(_motor.VerticalVelocity, 0f);

            yield return new WaitForSeconds(0.1f);
            _motor.UpdateMotor(0.1f);

            yield return new WaitForSeconds(0.8f);
            for (int i = 0; i < 20; i++)
            {
                _motor.UpdateMotor(0.05f);
            }

            Assert.IsTrue(_motor.IsGrounded);
        }

        [UnityTest]
        public IEnumerator Slide_SetsSlidingState_AndRestoresHeightAfterDuration()
        {
            _controller.HandleCommand(RunnerCommand.Slide);
            Assert.IsTrue(_motor.IsSliding);

            for (int i = 0; i < 20; i++)
            {
                _motor.UpdateMotor(0.05f);
            }

            Assert.IsFalse(_motor.IsSliding);
            yield return null;
        }
    }
}
