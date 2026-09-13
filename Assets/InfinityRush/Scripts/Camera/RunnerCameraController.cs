using UnityEngine;
using InfinityRush.Runner;

namespace InfinityRush.Camera
{
    public class RunnerCameraController : MonoBehaviour
    {
        [Header("Target & Offset")]
        [SerializeField] private Transform target;
        [SerializeField] private PlayerRunnerController runnerController;
        [SerializeField] private Vector3 defaultOffset = new Vector3(0f, 3.5f, -6.5f);
        [SerializeField] private Vector3 defaultRotation = new Vector3(15f, 0f, 0f);

        [Header("Follow Damping")]
        [SerializeField] private float followDampingX = 12f;
        [SerializeField] private float followDampingY = 8f;
        [SerializeField] private float followDampingZ = 15f;

        [Header("Dynamic Effects")]
        [SerializeField] private float laneTiltAngle = 3.5f;
        [SerializeField] private float speedZoomFovAddition = 8f;
        [SerializeField] private UnityEngine.Camera targetCamera;

        [Header("Accessibility")]
        public bool reduceCameraMotion = false;

        private float _baseFov = 60f;
        private Vector3 _currentVelocity;

        private void Awake()
        {
            if (targetCamera == null)
            {
                targetCamera = GetComponent<UnityEngine.Camera>() ?? UnityEngine.Camera.main;
            }
            if (targetCamera != null)
            {
                _baseFov = targetCamera.fieldOfView;
            }
        }

        public void SetTarget(Transform newTarget, PlayerRunnerController controller)
        {
            target = newTarget;
            runnerController = controller;
        }

        private void LateUpdate()
        {
            if (target == null) return;

            float dt = Time.deltaTime;

            // Target Position Calculation
            Vector3 targetPos = target.position + defaultOffset;

            if (reduceCameraMotion)
            {
                transform.position = targetPos;
                transform.rotation = Quaternion.Euler(defaultRotation);
                if (targetCamera != null) targetCamera.fieldOfView = _baseFov;
                return;
            }

            // Smooth Position Follow
            Vector3 currentPos = transform.position;
            currentPos.x = Mathf.Lerp(currentPos.x, targetPos.x, dt * followDampingX);
            currentPos.y = Mathf.Lerp(currentPos.y, targetPos.y, dt * followDampingY);
            currentPos.z = Mathf.Lerp(currentPos.z, targetPos.z, dt * followDampingZ);

            transform.position = currentPos;

            // Camera Tilt on Lane Switch
            float tilt = 0f;
            if (runnerController != null && runnerController.Motor != null)
            {
                float laneDiff = runnerController.Motor.CurrentX - runnerController.Motor.TargetX;
                tilt = laneDiff * laneTiltAngle;
            }

            Vector3 rot = defaultRotation;
            rot.z = tilt;
            transform.rotation = Quaternion.Euler(rot);

            // Dynamic FOV Zoom on Speed Boost
            if (targetCamera != null && runnerController != null && runnerController.Motor != null)
            {
                float targetFov = _baseFov;
                if (runnerController.Motor.IsFastDropping)
                {
                    targetFov += speedZoomFovAddition;
                }
                targetCamera.fieldOfView = Mathf.Lerp(targetCamera.fieldOfView, targetFov, dt * 6f);
            }
        }
    }
}
