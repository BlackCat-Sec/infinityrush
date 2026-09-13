using UnityEngine;
using UnityEngine.UI;
using InfinityRush.Core;
using InfinityRush.Scoring;

namespace InfinityRush.UI
{
    public class ResultsPresenter : MonoBehaviour
    {
        [Header("Results Displays")]
        [SerializeField] private Text finalScoreText;
        [SerializeField] private Text bestScoreText;
        [SerializeField] private Text coinsText;
        [SerializeField] private Text distanceText;
        [SerializeField] private GameObject newBestBadge;

        [Header("Action Buttons")]
        [SerializeField] private Button homeButton;
        [SerializeField] private Button restartButton;
        [SerializeField] private Button reviveButton;

        private void Start()
        {
            if (homeButton != null) homeButton.onClick.AddListener(OnHomeClicked);
            if (restartButton != null) restartButton.onClick.AddListener(OnRestartClicked);
            if (reviveButton != null) reviveButton.onClick.AddListener(OnReviveClicked);

            PopulateResults();
        }

        public void PopulateResults()
        {
            if (ServiceRegistry.Instance.TryGet<IScoreService>(out var scoreService))
            {
                var stats = scoreService.Stats;
                if (finalScoreText != null) finalScoreText.text = $"Score: {stats.currentScore}";
                if (bestScoreText != null) bestScoreText.text = $"Best: {stats.highScore}";
                if (coinsText != null) coinsText.text = $"+{stats.coinsCollected} Coins";
                if (distanceText != null) distanceText.text = $"{Mathf.FloorToInt(stats.distanceTravelled)} m";

                if (newBestBadge != null)
                {
                    newBestBadge.SetActive(stats.currentScore >= stats.highScore && stats.currentScore > 0);
                }
            }
        }

        private void OnHomeClicked()
        {
            if (ServiceRegistry.Instance.TryGet<GameStateMachine>(out var sm))
            {
                sm.ChangeState<MainMenuState>();
            }
        }

        private void OnRestartClicked()
        {
            if (ServiceRegistry.Instance.TryGet<GameStateMachine>(out var sm))
            {
                sm.ChangeState<RunnerState>();
            }
        }

        private void OnReviveClicked()
        {
            // Revive runner
        }
    }
}
