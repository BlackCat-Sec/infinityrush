using UnityEngine;
using UnityEngine.UI;

namespace InfinityRush.UI
{
    public class CharacterSelectPresenter : MonoBehaviour
    {
        [SerializeField] private Button backButton;
        [SerializeField] private Button selectButton;

        private void Start()
        {
            if (backButton != null) backButton.onClick.AddListener(OnBackClicked);
        }

        private void OnBackClicked()
        {
            gameObject.SetActive(false);
        }
    }
}
