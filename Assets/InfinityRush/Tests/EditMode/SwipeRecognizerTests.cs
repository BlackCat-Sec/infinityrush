using NUnit.Framework;
using InfinityRush.Input;

namespace InfinityRush.Tests.EditMode
{
    [TestFixture]
    public class SwipeRecognizerTests
    {
        private SwipeRecognizer _recognizer;
        private SwipeConfig _config;

        [SetUp]
        public void SetUp()
        {
            _config = new SwipeConfig
            {
                minSwipeDistancePixels = 50f,
                maxSwipeDurationSeconds = 0.4f,
                doubleTapMaxDelaySeconds = 0.3f
            };
            _recognizer = new SwipeRecognizer(_config);
        }

        [Test]
        public void SwipeLeft_RecognizesMoveLeft()
        {
            _recognizer.OnTouchDown(200f, 200f, 0.0f);
            var result = _recognizer.OnTouchUp(100f, 200f, 0.1f);
            Assert.AreEqual(RunnerCommand.MoveLeft, result);
        }

        [Test]
        public void SwipeRight_RecognizesMoveRight()
        {
            _recognizer.OnTouchDown(100f, 200f, 0.0f);
            var result = _recognizer.OnTouchUp(200f, 200f, 0.1f);
            Assert.AreEqual(RunnerCommand.MoveRight, result);
        }

        [Test]
        public void SwipeUp_RecognizesJump()
        {
            _recognizer.OnTouchDown(200f, 100f, 0.0f);
            var result = _recognizer.OnTouchUp(200f, 250f, 0.1f);
            Assert.AreEqual(RunnerCommand.Jump, result);
        }

        [Test]
        public void SwipeDown_RecognizesSlide()
        {
            _recognizer.OnTouchDown(200f, 250f, 0.0f);
            var result = _recognizer.OnTouchUp(200f, 100f, 0.1f);
            Assert.AreEqual(RunnerCommand.Slide, result);
        }

        [Test]
        public void SlowSwipe_ExceedsMaxDuration_ReturnsNone()
        {
            _recognizer.OnTouchDown(100f, 200f, 0.0f);
            var result = _recognizer.OnTouchUp(200f, 200f, 1.0f); // 1s > 0.4s
            Assert.AreEqual(RunnerCommand.None, result);
        }

        [Test]
        public void DoubleTap_RecognizesActivateHoverboard()
        {
            _recognizer.OnTouchDown(200f, 200f, 0.0f);
            _recognizer.OnTouchUp(200f, 200f, 0.05f); // 1st tap

            _recognizer.OnTouchDown(200f, 200f, 0.15f);
            var result = _recognizer.OnTouchUp(200f, 200f, 0.20f); // 2nd tap within 0.3s
            Assert.AreEqual(RunnerCommand.ActivateHoverboard, result);
        }
    }
}
