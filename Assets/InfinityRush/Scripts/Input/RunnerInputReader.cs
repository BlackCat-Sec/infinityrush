using System;
using UnityEngine;
using InfinityRush.Core;

namespace InfinityRush.Input
{
    public interface IInputReader
    {
        event Action<RunnerCommand> OnCommand;
    }

    public class RunnerInputReader : MonoBehaviour, IInputReader
    {
        public event Action<RunnerCommand> OnCommand;

        [SerializeField] private float minSwipeDistancePixels = 40f;
        [SerializeField] private float maxSwipeDurationSeconds = 0.5f;

        private SwipeRecognizer _swipeRecognizer;

        private void Awake()
        {
            var config = new SwipeConfig
            {
                minSwipeDistancePixels = minSwipeDistancePixels,
                maxSwipeDurationSeconds = maxSwipeDurationSeconds,
                doubleTapMaxDelaySeconds = 0.32f
            };
            _swipeRecognizer = new SwipeRecognizer(config);

            ServiceRegistry.Instance.Register<IInputReader>(this);
        }

        private void Update()
        {
            HandleKeyboardInput();
            HandleTouchMouseInput();
        }

        private void HandleKeyboardInput()
        {
            if (UnityEngine.Input.GetKeyDown(KeyCode.A) || UnityEngine.Input.GetKeyDown(KeyCode.LeftArrow))
            {
                SendCommand(RunnerCommand.MoveLeft);
            }
            else if (UnityEngine.Input.GetKeyDown(KeyCode.D) || UnityEngine.Input.GetKeyDown(KeyCode.RightArrow))
            {
                SendCommand(RunnerCommand.MoveRight);
            }
            else if (UnityEngine.Input.GetKeyDown(KeyCode.W) || UnityEngine.Input.GetKeyDown(KeyCode.UpArrow) || UnityEngine.Input.GetKeyDown(KeyCode.Space))
            {
                SendCommand(RunnerCommand.Jump);
            }
            else if (UnityEngine.Input.GetKeyDown(KeyCode.S) || UnityEngine.Input.GetKeyDown(KeyCode.DownArrow))
            {
                SendCommand(RunnerCommand.Slide);
            }
            else if (UnityEngine.Input.GetKeyDown(KeyCode.E))
            {
                SendCommand(RunnerCommand.ActivateHoverboard);
            }
        }

        private void HandleTouchMouseInput()
        {
            float now = Time.unscaledTime;

            if (UnityEngine.Input.GetMouseButtonDown(0))
            {
                Vector3 pos = UnityEngine.Input.mousePosition;
                _swipeRecognizer.OnTouchDown(pos.x, pos.y, now);
            }
            else if (UnityEngine.Input.GetMouseButton(0))
            {
                Vector3 pos = UnityEngine.Input.mousePosition;
                var cmd = _swipeRecognizer.ProcessDrag(pos.x, pos.y, now);
                if (cmd != RunnerCommand.None)
                {
                    SendCommand(cmd);
                }
            }
            else if (UnityEngine.Input.GetMouseButtonUp(0))
            {
                Vector3 pos = UnityEngine.Input.mousePosition;
                var cmd = _swipeRecognizer.OnTouchUp(pos.x, pos.y, now);
                if (cmd != RunnerCommand.None)
                {
                    SendCommand(cmd);
                }
            }
        }

        public void SendCommand(RunnerCommand command)
        {
            if (command != RunnerCommand.None)
            {
                OnCommand?.Invoke(command);
            }
        }

        private void OnDestroy()
        {
            ServiceRegistry.Instance.Unregister<IInputReader>();
        }
    }
}
