using System;

namespace InfinityRush.Input
{
    public struct SwipeConfig
    {
        public float minSwipeDistancePixels;
        public float maxSwipeDurationSeconds;
        public float doubleTapMaxDelaySeconds;

        public static SwipeConfig Default => new SwipeConfig
        {
            minSwipeDistancePixels = 40f,
            maxSwipeDurationSeconds = 0.5f,
            doubleTapMaxDelaySeconds = 0.3f
        };
    }

    public class SwipeRecognizer
    {
        private SwipeConfig _config;
        private bool _isTracking;
        private float _startX;
        private float _startY;
        private float _startTime;
        private float _lastTapTime;

        public SwipeRecognizer(SwipeConfig config)
        {
            _config = config;
            _isTracking = false;
            _lastTapTime = -100f;
        }

        public void UpdateConfig(SwipeConfig config)
        {
            _config = config;
        }

        public void OnTouchDown(float x, float y, float currentTime)
        {
            _isTracking = true;
            _startX = x;
            _startY = y;
            _startTime = currentTime;
        }

        public RunnerCommand OnTouchUp(float endX, float endY, float currentTime)
        {
            if (!_isTracking)
                return RunnerCommand.None;

            _isTracking = false;
            float duration = currentTime - _startTime;
            float deltaX = endX - _startX;
            float deltaY = endY - _startY;
            float absX = Math.Abs(deltaX);
            float absY = Math.Abs(deltaY);

            if (duration > _config.maxSwipeDurationSeconds)
                return RunnerCommand.None;

            if (absX >= _config.minSwipeDistancePixels || absY >= _config.minSwipeDistancePixels)
            {
                if (absX > absY)
                {
                    return deltaX > 0 ? RunnerCommand.MoveRight : RunnerCommand.MoveLeft;
                }
                else
                {
                    return deltaY > 0 ? RunnerCommand.Jump : RunnerCommand.Slide;
                }
            }

            if (currentTime - _lastTapTime <= _config.doubleTapMaxDelaySeconds)
            {
                _lastTapTime = -100f;
                return RunnerCommand.ActivateHoverboard;
            }

            _lastTapTime = currentTime;
            return RunnerCommand.None;
        }

        public RunnerCommand ProcessDrag(float currentX, float currentY, float currentTime)
        {
            if (!_isTracking)
                return RunnerCommand.None;

            float deltaX = currentX - _startX;
            float deltaY = currentY - _startY;
            float absX = Math.Abs(deltaX);
            float absY = Math.Abs(deltaY);

            if (absX >= _config.minSwipeDistancePixels || absY >= _config.minSwipeDistancePixels)
            {
                _isTracking = false;
                if (absX > absY)
                {
                    return deltaX > 0 ? RunnerCommand.MoveRight : RunnerCommand.MoveLeft;
                }
                else
                {
                    return deltaY > 0 ? RunnerCommand.Jump : RunnerCommand.Slide;
                }
            }

            return RunnerCommand.None;
        }

        public void Cancel()
        {
            _isTracking = false;
        }
    }
}
