using UnityEngine;
using UnityEngine.UI;
using InfinityRush.Core;
using InfinityRush.Audio;

namespace InfinityRush.UI
{
    public class SettingsPresenter : MonoBehaviour
    {
        [SerializeField] private Slider musicSlider;
        [SerializeField] private Slider sfxSlider;
        [SerializeField] private Toggle reduceMotionToggle;
        [SerializeField] private Button closeButton;

        private void Start()
        {
            if (closeButton != null) closeButton.onClick.AddListener(OnCloseClicked);
            if (musicSlider != null) musicSlider.onValueChanged.AddListener(OnMusicChanged);
            if (sfxSlider != null) sfxSlider.onValueChanged.AddListener(OnSfxChanged);
        }

        private void OnMusicChanged(float val)
        {
            if (ServiceRegistry.Instance.TryGet<AudioManager>(out var audio))
            {
                audio.SetMusicVolume(val);
            }
        }

        private void OnSfxChanged(float val)
        {
            if (ServiceRegistry.Instance.TryGet<AudioManager>(out var audio))
            {
                audio.SetSfxVolume(val);
            }
        }

        private void OnCloseClicked()
        {
            gameObject.SetActive(false);
        }
    }
}
