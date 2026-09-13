using UnityEngine;
using UnityEngine.UI;
using InfinityRush.Core;
using InfinityRush.Scoring;

namespace InfinityRush.UI
{
    public class HudPresenter : MonoBehaviour
    {
        [Header("Top Bar UI")]
        [SerializeField] private Text scoreText;
        [SerializeField] private Text distanceText;
        [SerializeField] private Text coinsText;
        [SerializeField] private Text multiplierText;
        [SerializeField] private Button pauseButton;

        [Header("PowerUp Active Bars")]
        [SerializeField] private GameObject magnetBar;
        [SerializeField] private GameObject shieldBar;
        [SerializeField] private GameObject multiplierBar;

        private IScoreService _scoreService;

        private void Start()
        {
            if (ServiceRegistry.Instance.TryGet<IScoreService>(out _scoreService))
            {
                _scoreService.OnScoreChanged += UpdateScoreUI;
                _scoreService.OnCoinsChanged += UpdateCoinsUI;
                _scoreService.OnMultiplierChanged += UpdateMultiplierUI;

                UpdateScoreUI(_scoreService.CurrentScore);
                UpdateCoinsUI(_scoreService.CoinsCollected);
                UpdateMultiplierUI(_scoreService.Multiplier);
            }

            if (pauseButton != null)
            {
                pauseButton.onClick.AddListener(OnPauseClicked);
            }
        }

        private void UpdateScoreUI(int score)
        {
            if (scoreText != null) scoreText.text = $"Score {score}";
        }

        private void UpdateCoinsUI(int coins)
        {
            if (coinsText != null) coinsText.text = $"Coins {coins}";
        }

        private void UpdateMultiplierUI(int multiplier)
        {
            if (multiplierText != null) multiplierText.text = multiplier > 1 ? $"{multiplier}X" : "";
        }

        public void UpdateDistanceUI(float distanceMeters)
        {
            if (distanceText != null) distanceText.text = $"{Mathf.FloorToInt(distanceMeters)} m";
        }

        private void OnPauseClicked()
        {
            if (ServiceRegistry.Instance.TryGet<GameStateMachine>(out var sm))
            {
                sm.ChangeState<PausedState>();
            }
        }

        private void OnDestroy()
        {
            if (_scoreService != null)
            {
                _scoreService.OnScoreChanged -= UpdateScoreUI;
                _scoreService.OnCoinsChanged -= UpdateCoinsUI;
                _scoreService.OnMultiplierChanged -= UpdateMultiplierUI;
            }
        }
    }
}
