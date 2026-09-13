using System;

namespace InfinityRush.Meta
{
    [Serializable]
    public class Wallet
    {
        public int softCurrencyCoins;
        public int hardCurrencyGems;

        public event Action<int> OnCoinsChanged;
        public event Action<int> OnGemsChanged;

        public void AddCoins(int amount)
        {
            if (amount <= 0) return;
            softCurrencyCoins += amount;
            OnCoinsChanged?.Invoke(softCurrencyCoins);
        }

        public bool SpendCoins(int amount)
        {
            if (amount <= 0 || softCurrencyCoins < amount) return false;
            softCurrencyCoins -= amount;
            OnCoinsChanged?.Invoke(softCurrencyCoins);
            return true;
        }

        public void AddGems(int amount)
        {
            if (amount <= 0) return;
            hardCurrencyGems += amount;
            OnGemsChanged?.Invoke(hardCurrencyGems);
        }

        public bool SpendGems(int amount)
        {
            if (amount <= 0 || hardCurrencyGems < amount) return false;
            hardCurrencyGems -= amount;
            OnGemsChanged?.Invoke(hardCurrencyGems);
            return true;
        }
    }
}
