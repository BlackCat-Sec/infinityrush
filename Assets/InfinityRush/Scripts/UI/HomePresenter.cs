using UnityEngine;
using UnityEngine.UI;
using InfinityRush.Core;

namespace InfinityRush.UI
{
    public class HomePresenter : MonoBehaviour
    {
        [Header("Menu Buttons")]
        [SerializeField] private Button playButton;
        [SerializeField] private Button charactersButton;
        [SerializeField] private Button boardsButton;
        [SerializeField] private Button missionsButton;
        [SerializeField] private Button shopButton;
        [SerializeField] private Button settingsButton;

        [Header("Header Info")]
        [SerializeField] private Text coinsText;
        [SerializeField] private Text gemsText;
        [SerializeField] private Text levelText;

        private void Start()
        {
            if (playButton != null) playButton.onClick.AddListener(OnPlayClicked);
            if (settingsButton != null) settingsButton.onClick.AddListener(OnSettingsClicked);
        }

        private void OnPlayClicked()
        {
            if (ServiceRegistry.Instance.TryGet<GameStateMachine>(out var sm))
            {
                sm.ChangeState<RunnerState>();
            }
        }

        private void OnSettingsClicked()
        {
            // Opens settings dialog
        }
    }
}
