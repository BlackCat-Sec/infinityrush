using UnityEngine;
using UnityEngine.UI;

namespace InfinityRush.UI
{
    public class ShopPresenter : MonoBehaviour
    {
        [SerializeField] private Button backButton;

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
